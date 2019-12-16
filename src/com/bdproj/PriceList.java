package com.bdproj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;


public class PriceList {
    private SystemUser systemUser;
    private String lastError;

    private final String UNIT = "zł/pkt";
    private final int PRESENT_PRICE_LIST_ID = 0;

    private String selectedPriceListId = null;
    protected enum PriceListEnum { PRICE_LIST_DICTIONARY_ID, NAME, PRICE };
    protected ArrayList<EnumMap<PriceListEnum, String>> selectedPriceList;

    protected enum PriceListsHeadersEnum { NAME, ID, SINCE, SUPERVISOR_ID, SUPERVISOR_NAME };
    protected ArrayList<EnumMap<PriceListsHeadersEnum, String>> priceListsHeadersList;

    PriceList(SystemUser user) {
        systemUser = user;
        fetchPriceListsHeaders();
        fetchSinglePriceList();
    }

    public boolean fetchPriceListsHeaders() {

        String query =  "select c.id, (case when c.od < now() then 'Obecny' else concat('Ważny od ', str_to_date(c.od, '%Y-%m-%d')) end) as nazwa, DATE_FORMAT(c.od, '%Y-%m-%d %H:%i') as od, \n" +
                        "c.kierownik_id, concat(k.imie, ' ', k.nazwisko) as 'kierownik'\n" +
                        "from cennik c join kierownik k on c.kierownik_id = k.id\n" +
                        "where c.id = (select c1.id from cennik c1 where c1.od < now() and c1.odw_przed_wej=0 order by UNIX_TIMESTAMP(c1.od) desc limit 1) or\n" +
                        "c.id = any (select c2.id from cennik c2 where c2.od > now() and c2.odw_przed_wej=0) order by UNIX_TIMESTAMP(c.od);";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            priceListsHeadersList = new ArrayList<>();
            while (rs.next()) {
                EnumMap<PriceListsHeadersEnum, String> tmp = new EnumMap<>(PriceListsHeadersEnum.class);
                tmp.put(PriceListsHeadersEnum.ID, rs.getString("id"));
                tmp.put(PriceListsHeadersEnum.NAME, rs.getString("nazwa"));
                tmp.put(PriceListsHeadersEnum.SINCE, rs.getString("od"));
                tmp.put(PriceListsHeadersEnum.SUPERVISOR_ID, rs.getString("kierownik_id"));
                tmp.put(PriceListsHeadersEnum.SUPERVISOR_NAME, rs.getString("kierownik"));
                priceListsHeadersList.add(tmp);
            }
            if(priceListsHeadersList.isEmpty()) {
                lastError = "Brak cenników w bazie";
            }
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    public boolean fetchSinglePriceList() {
        if(selectedPriceListId == null && !priceListsHeadersList.isEmpty()) {
            lastError = "Nie wybrano żadnego cennika.";
            return false;
        }
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        String query = "";

        if(priceListsHeadersList.isEmpty()) {
            // baza jest pusta.
            query = "select sc.id as slownik_cennik_id, sc.nazwa, '' as cena\n" +
                    "from slownik_cennik sc where sc.id > 0/?;";
        }
        else {
            query = "select pc.slownik_cennik_id, sc.nazwa, pc.cena\n" +
                    "from poz_cennik pc join slownik_cennik sc on pc.slownik_cennik_id = sc.id\n" +
                    "where pc.cennik_id = ?;";
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1, Integer.parseInt(selectedPriceListId));
            ResultSet rs = ps.executeQuery();
            selectedPriceList = new ArrayList<>();
            while (rs.next()) {
                EnumMap<PriceListEnum, String> tmp = new EnumMap<>(PriceListEnum.class);
                tmp.put(PriceListEnum.PRICE_LIST_DICTIONARY_ID, rs.getString("slownik_cennik_id"));
                tmp.put(PriceListEnum.NAME, rs.getString("nazwa"));
                tmp.put(PriceListEnum.PRICE, rs.getString("cena"));
                selectedPriceList.add(tmp);
            }
            if(selectedPriceList.isEmpty()) {
                lastError = "Brak pozycji cennika dla cennika o numerze " + selectedPriceListId;
            }
            else return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    public void setSelectedPriceList(String name) {
        EnumMap<PriceListsHeadersEnum, String> selected = priceListsHeadersList
                .stream()
                .filter(item -> item.get(PriceListsHeadersEnum.NAME).equals(name))
                .findAny()
                .orElse(null);
        if(selected == null) {
            selectedPriceListId = null;
        }
        else {
            selectedPriceListId = selected.get(PriceListsHeadersEnum.ID);
        }
    }

    public boolean hasModifyPrivileges() {

        if(priceListsHeadersList == null || selectedPriceList == null || selectedPriceListId == null) {  // Nie pobrano danych z bazy
            return false;
        }
        if(priceListsHeadersList.isEmpty()) {   // potencjalnie pusta baza
            return true;
        }
        if(isPresentPriceList()) {              // bieżący cennik każdy może "edytować" i zapisać jako nowy.
            return true;
        }
        EnumMap<PriceListsHeadersEnum, String> presentPriceListHeader = priceListsHeadersList.stream().filter(item -> item.get(PriceListsHeadersEnum.ID).equals(selectedPriceListId)).findAny().orElse(null);
        if(presentPriceListHeader == null) {
            return false;
        }
        if(String.valueOf(systemUser.getId()).equals(presentPriceListHeader.get(PriceListsHeadersEnum.SUPERVISOR_ID))) { // bieżący kierownik jest właścicielem nowego cennika.
            return true;
        }
        return false;
    }

    public boolean isPresentPriceList() {
        return priceListsHeadersList.get(PRESENT_PRICE_LIST_ID).get(PriceListsHeadersEnum.ID).equals(selectedPriceListId);
    }

    public String getLastError() {
        return lastError;
    }

    public String getUnit() {
        return UNIT;
    }

    public EnumMap<PriceListsHeadersEnum, String> getCurrentHeader() {
        if(priceListsHeadersList == null || selectedPriceListId == null) {
            return null;
        }
        return priceListsHeadersList.stream().filter(item -> item.get(PriceListsHeadersEnum.ID).equals(selectedPriceListId)).findFirst().orElse(null);
    }

    public String getAuthor() {
        EnumMap<PriceListsHeadersEnum, String> currentHeader = getCurrentHeader();
        return currentHeader == null ? "" : currentHeader.get(PriceListsHeadersEnum.SUPERVISOR_NAME);
    }

    public String getValidSince() {
        EnumMap<PriceListsHeadersEnum, String> currentHeader = getCurrentHeader();
        return currentHeader == null ? "" : currentHeader.get(PriceListsHeadersEnum.SINCE);
    }

    public String getCurrentName() {
        EnumMap<PriceListsHeadersEnum, String> currentHeader = getCurrentHeader();
        return currentHeader == null ? "" : currentHeader.get(PriceListsHeadersEnum.NAME);
    }

    public boolean checkIfIsUniqueDate() {
        return false;
    }



/*
    public void setPriceListPrices(ArrayList<Double> priceListPrices) {
        this.priceListPricesNew = priceListPrices;
    }

    public boolean createNewPriceList() {

        PreparedStatement ps;
        ResultSet rs;
        String query1 = "insert into cennik set od=now(), kierownik_id=?;";
        String query2 = "select CONCAT(k.imie, ' ', k.nazwisko) as 'autor', DATE_FORMAT(c.od, '%Y-%m-%d %H:%i') as 'od', c.id as 'cennik_id' \n" +
                        "from cennik c join kierownik k on c.kierownik_id = k.id \n" +
                        "where c.id = (select max(c1.id) from cennik c1);";
        String query3 = "insert into poz_cennik(cena, cennik_id, slownik_cennik_id) values(?, ?, ?);";

        if (!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            ps = MySQLConnection.getConnection().prepareStatement(query1);
            ps.setInt(1, systemUser.getId());
            ps.execute();
            rs = ps.executeQuery(query2);

            if(rs.first()) {
                this.author = rs.getString("autor");
                this.validSince = rs.getString("od");
                this.priceListId = rs.getInt("cennik_id");

                ps = MySQLConnection.getConnection().prepareStatement(query3);

                for (int i = 0; i < priceListIds.size(); i++) {
                    ps.setDouble(1, priceListPricesNew.get(i));
                    ps.setInt(2, priceListId);
                    ps.setInt(3, priceListIds.get(i));
                    ps.addBatch();
                }

                ps.executeBatch();
                priceListPrices = priceListPricesNew;
                priceListPricesNew = null;
                return true;
            }
            lastError = "Can not get new price list id.";
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

 */
}
