package com.bdproj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;


public class PriceList {
    private SystemUser systemUser;
    private String lastError;

    private final String UNIT = "zł/pkt";
    private final int PRESENT_PRICE_LIST_ID = 0;
    protected final String DATE_FORMAT = "yyyy-MM-dd";

    private boolean dataBaseIsEmpty = false;

    private String selectedPriceListId = null;
    public enum PriceListEnum { PRICE_LIST_DICTIONARY_ID, NAME, PRICE };
    protected ArrayList<EnumMap<PriceListEnum, String>> selectedPriceList;

    public enum PriceListsHeadersEnum { NAME, ID, SINCE, SUPERVISOR_ID, SUPERVISOR_NAME };
    protected ArrayList<EnumMap<PriceListsHeadersEnum, String>> priceListsHeadersList;

    PriceList(SystemUser user) {
        systemUser = user;
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
            dataBaseIsEmpty = priceListsHeadersList.isEmpty();
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

        if(dataBaseIsEmpty) {
            // baza jest pusta.
            query = "select sc.id as slownik_cennik_id, sc.nazwa, '' as cena\n" +
                    "from slownik_cennik sc;";
            selectedPriceListId = "0";
        }
        else {
            query = "select pc.slownik_cennik_id, sc.nazwa, round(pc.cena, 2) as cena\n" +
                    "from poz_cennik pc join slownik_cennik sc on pc.slownik_cennik_id = sc.id\n" +
                    "where pc.cennik_id = ?;";
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            if(!dataBaseIsEmpty) ps.setInt(1, Integer.parseInt(selectedPriceListId));
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

    public boolean isDataBaseIsEmpty() {
        return dataBaseIsEmpty;
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
        if(dataBaseIsEmpty) {   // potencjalnie pusta baza
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
        return dataBaseIsEmpty || priceListsHeadersList.get(PRESENT_PRICE_LIST_ID).get(PriceListsHeadersEnum.ID).equals(selectedPriceListId);
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

    public String getValidTo() {
       EnumMap<PriceListsHeadersEnum, String> validTo = priceListsHeadersList.stream().filter(item -> {
            try {
                Date validSince = (new SimpleDateFormat(DATE_FORMAT)).parse(getValidSince());
                return (new SimpleDateFormat(DATE_FORMAT)).parse(item.get(PriceListsHeadersEnum.SINCE)).after(validSince);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }).findFirst().orElse(null);

       return validTo == null ? "-" : validTo.get(PriceListsHeadersEnum.SINCE);
    }

    public String getCurrentName() {
        EnumMap<PriceListsHeadersEnum, String> currentHeader = getCurrentHeader();
        return currentHeader == null ? "" : currentHeader.get(PriceListsHeadersEnum.NAME);
    }

    public boolean checkIfDateIsUnique(Date date) {
        if(!fetchPriceListsHeaders()) return false;

        EnumMap<PriceListsHeadersEnum, String> similarItem = priceListsHeadersList
                .stream()
                .filter(
                        item -> {
                            try {
                                return (
                                        (new SimpleDateFormat(DATE_FORMAT)).parse(item.get(PriceListsHeadersEnum.SINCE)).compareTo(date) == 0 &&
                                         !item.get(PriceListsHeadersEnum.ID).equals(selectedPriceListId)
                                );
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                            return false;
                        }
                )
                .findAny()
                .orElse(null);

        if(similarItem == null) return true;

        lastError = "Podana data istnieje już w systemie, spróbuj wybrać inną.";
        return false;
    }

    public boolean saveAsNewPriceList(Date validSince, ArrayList<EnumMap<PriceListEnum, String>> newPriceListItems) {

        String query1 = "insert into cennik set od=?, kierownik_id=?;";
        String query2 = "insert into poz_cennik(cena, cennik_id, slownik_cennik_id) values(?, ?, ?);";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            int insertedId;
            PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1, Statement.RETURN_GENERATED_KEYS);
            ps1.setString(1, (new SimpleDateFormat(DATE_FORMAT)).format(validSince));
            ps1.setInt(2, systemUser.getId());
            ps1.executeUpdate();
            ResultSet rs1 = ps1.getGeneratedKeys();
            if(rs1.next()) {
                insertedId = rs1.getInt(1);
            }
            else return false;

            PreparedStatement ps2 = MySQLConnection.getConnection().prepareStatement(query2);
            for(EnumMap<PriceListEnum, String> item : newPriceListItems) {
                ps2.setDouble(1, Double.parseDouble(item.get(PriceListEnum.PRICE)));
                ps2.setInt(2, insertedId);
                ps2.setInt(3, Integer.parseInt(item.get(PriceListEnum.PRICE_LIST_DICTIONARY_ID)));
                ps2.addBatch();
            }
            ps2.executeBatch();
            selectedPriceListId = String.valueOf(insertedId);
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    public boolean updateCurrentPriceList(Date validSince, ArrayList<EnumMap<PriceListEnum, String>> modifiedPriceListItems) {

        String query1 = "update cennik set od=? where id=?";
        String query2 = "update poz_cennik set cena=? where cennik_id=? and slownik_cennik_id=?";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1);
            ps1.setString(1, (new SimpleDateFormat(DATE_FORMAT)).format(validSince));
            ps1.setInt(2, Integer.parseInt(selectedPriceListId));
            ps1.executeUpdate();

            PreparedStatement ps2 = MySQLConnection.getConnection().prepareStatement(query2);
            for(EnumMap<PriceListEnum, String> item : modifiedPriceListItems) {
                ps2.setDouble(1, Double.parseDouble(item.get(PriceListEnum.PRICE)));
                ps2.setInt(2, Integer.parseInt(selectedPriceListId));
                ps2.setInt(3, Integer.parseInt(item.get(PriceListEnum.PRICE_LIST_DICTIONARY_ID)));
                ps2.addBatch();
            }
            ps2.executeBatch();

            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    public boolean deleteCurrentPriceList() {
        String query = "update cennik set odw_przed_wej=1 where id=?;";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1, Integer.parseInt(selectedPriceListId));
            ps.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }
}
