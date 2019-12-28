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

/**
 * Klasa odpowiedzialna za obsługę cennika.
 */
public class PriceList {
    /**
     * Obiekt obecnie zalogowanego użytkownika systemu.
     */
    private SystemUser systemUser;
    /**
     * Opis ostatniego błędu.
     */
    private String lastError;

    /**
     * Jednostka cen w cenniku.
     */
    private final String UNIT = "zł/pkt";
    /**
     * Numer id obecnie obowiązującego cennika w liście nagłówków cennika.
     */
    private final int PRESENT_PRICE_LIST_ID = 0;
    /**
     * Format daty.
     */
    protected final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Flaga ustawiana jeżeli brak cenników w bazie danych.
     */
    private boolean dataBaseIsEmpty = false;

    /**
     * Numer id obecnie wybranego cennika.
     */
    private String selectedPriceListId = null;

    /**
     * Nazwy danych cennika przechowywanych lokalnie.
     */
    public enum PriceListEnum {
        /**
         * Numer id pozycji cennika w słowniku.
         */
        PRICE_LIST_DICTIONARY_ID,
        /**
         * Nazwa pozycji cennika.
         */
        NAME,
        /**
         * Cena definiowana przez pozycję cennika.
         */
        PRICE
    };
    /**
     * Lista zawierająca dane obecnie wybranego cennika.
     */
    protected ArrayList<EnumMap<PriceListEnum, String>> selectedPriceList;

    /**
     * Nazwy danych o cennikach przechowywanych lokalnie. Pierwszy cennik to obecny, kolejne w porządku rosnącym względem daty wejścia w życie.
     */
    public enum PriceListsHeadersEnum {
        /**
         * Nazwa cennika.
         */
        NAME,
        /**
         * Numer id cennika.
         */
        ID,
        /**
         * Data wejścia w życie cennika.
         */
        SINCE,
        /**
         * Numer id kierownika który stworzył cennik.
         */
        SUPERVISOR_ID,
        /**
         * Nazwa kierownika.
         */
        SUPERVISOR_NAME
    };
    /**
     * Lista przechowywująca dane o cennikach.
     */
    protected ArrayList<EnumMap<PriceListsHeadersEnum, String>> priceListsHeadersList;

    /**
     * Domyślny konstruktor.
     * @param user Obiekt użytkownika systemu obecnie zalogowanego.
     */
    PriceList(SystemUser user) {
        systemUser = user;
    }

    /**
     * Metoda pobierająca informacje o cennikach z bazy.
     * @return Zwraca true jeżeli operacja zakońcyła się pomyślnie.
     */
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

    /**
     * Metoda pobierająca szczegóły obecnie wybranego cennika.
     * @return Zwraca true jeżeli operacja zakońcyła się pomyślnie.
     */
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

    /**
     * Metoda weryfikująca czy istnieją w bazie zdefiniowane cenniki.
     * @return Zwraca true jeżeli w bazie brak zdefiniowanych cenników.
     */
    public boolean isDataBaseIsEmpty() {
        return dataBaseIsEmpty;
    }

    /**
     * Setter.
     * @param name Nazwa nowo wybranego cennika.
     */
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

    /**
     * Metoda weryfikująca czy bieżący użytkownik ma prawa do edycji obecnie wybranego cennika.
     * @return Zwraca true jeżeli:
     *             <li>brak w bazie jakiegokolwiek zdefiniowanego cennika.</li>
     *             <li>wybrany cennik jest obecnie obowiązującym. Każdy kierownik może zapisać jako nowy.</li>
     *             <li>wybrany cennik nie jest obecnie obowiązującym, ale został stworzony przez obecnie zalogowanego użytkownika systemu.</li>
     */
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

    /**
     * Metoda weryfikująca czy wybrany cennik jest obecnie obowiązującym cennikiem.
     * @return
     */
    public boolean isPresentPriceList() {
        return dataBaseIsEmpty || priceListsHeadersList.get(PRESENT_PRICE_LIST_ID).get(PriceListsHeadersEnum.ID).equals(selectedPriceListId);
    }

    /**
     * Getter.
     * @return Zwraca opis ostatniego błędu.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Getter.
     * @return Zwraca jednostkę cen w cenniku.
     */
    public String getUnit() {
        return UNIT;
    }

    /**
     * Getter.
     * @return Zwraca wszystkie lokalnie przechowywane dane o obecnie wybranym cenniku.
     */
    public EnumMap<PriceListsHeadersEnum, String> getCurrentHeader() {
        if(priceListsHeadersList == null || selectedPriceListId == null) {
            return null;
        }
        return priceListsHeadersList
                .stream()
                .filter(
                        item -> item.get(PriceListsHeadersEnum.ID).equals(selectedPriceListId)
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * Getter.
     * @return Zwraca nazwę autora obecnie wybranego cennika.
     */
    public String getAuthor() {
        EnumMap<PriceListsHeadersEnum, String> currentHeader = getCurrentHeader();
        return currentHeader == null ? "" : currentHeader.get(PriceListsHeadersEnum.SUPERVISOR_NAME);
    }

    /**
     * Getter.
     * @return Zwraca datę od kiedy obecenie wybrany cennik obowiązuje / zacznie obowiązywać.
     */
    public String getValidSince() {
        EnumMap<PriceListsHeadersEnum, String> currentHeader = getCurrentHeader();
        return currentHeader == null ? "" : currentHeader.get(PriceListsHeadersEnum.SINCE);
    }

    /**
     * Getter.
     * @return Zwraca datę końca obowiązywania wybranego cennika, lub '-' jeżeli brak.
     */
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

    /**
     * Getter.
     * @return Zwraca nazwę wybranego cennika.
     */
    public String getCurrentName() {
        EnumMap<PriceListsHeadersEnum, String> currentHeader = getCurrentHeader();
        return currentHeader == null ? "" : currentHeader.get(PriceListsHeadersEnum.NAME);
    }

    /**
     * Metoda walidująca czy istnieje niewycofany cennik o dacie wejścia w życie takiej samej jak podana data.
     * @param date Data której należy wyszukać w bazie.
     * @return Zwraca true jeżeli brak w bazie cennika o podanej dacie.
     */
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

    /**
     * Metoda zapisująca nowy cennik.
     * @param validSince Data wejścia w życie nowego cennika.
     * @param newPriceListItems Pozycje nowego cennika.
     * @return Zwaraca true jeżeli operacja zakończyła się pomyślnie.
     */
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

    /**
     * Metoda aktualizująca pozycje wybranego cennika.
     * @param validSince Nowa data wejścia w życie.
     * @param modifiedPriceListItems Nowe pozycje cennika.
     * @return Zwraca true jeżeli operacja zakończyła się pomyślnie.
     */
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

    /**
     * Metoda ustawiająca flagę w bazie oznaczjącą wycofanie cennika przed wejściem w życie.
     * @return Zwraca true jeżeli operacja zakończyła się pomyślnie.
     */
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
