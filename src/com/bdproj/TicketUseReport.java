package com.bdproj;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Klasa służąca do generacji raportu użyć biletu.
 * Raport zawiera:
 *
 *                                                                  Data generacji raportu: yyyy-MM-dd hh:mm:ss
 *                                                                        Wygenerowano dla: Imie Nazwisko
 *
 *
 *                                                    Raport użyć biletu
 *
 *      Numer biletu: numer.
 *      Data zakupu biletu: yyyy-MM-dd hh:mm:ss.
 *      Data ostatniego daladowania: yyyy-MM-dd hh:mm:ss.
 *      Pozycja cennika     |   Cena jedn.  |   Liczba doładowań      |       Suma punktów       |        Lączna kwota
 *
 *      Liczba użyć: n, wydano punktów: o.
 *
 *      Stan konta: p punktów.
 *
 *      Przewyższenie całkowite: r metrów.
 *
 *      Wyciąg          |   Wydane punkty   |   Przewyższenie
 *      Wykres z przewyższeniami.
 *
 * @see HtmlReport
 */

public class TicketUseReport implements HtmlReport {

    /**
     * Ścieżka do pliku z szablonem w HTML.
     */
    private final String HTML_TEMPLATE_PATH = "reports/templates/TicketUseTemplate.html";

    private SystemUser systemUser;      /**< Obiekt użytkownika systemu. */
    private Integer ticketId;           /**< Numer id biletu. */
    private String htmlReport = null;   /**< Raport sformatowany zgodnie z szablonem HTML. */
    private String lastError = null;    /**< Opis ostatniego błędu. */

    private final Integer ID_PARAM_POSITION = 1;    /**< Pozycja paramteru (numeru id biletu) w zapytaniach SQL. */

    /**
     * Nazwy atrybutów zwracanych przez zapytanie numer 1.
     */
    private enum Query1Enum {
        TIMESTAMP   /**< Stempelczasowy generacji raportu. */
    };
    private EnumMap<Query1Enum, String> query1Map;          /**< Wyniki zapytania nr. 1. */
    private final String QUERY_1_GENERATION_TIME_STAMP =    /**< Treść zapytania nr. 1. */
            "select DATE_FORMAT(now(), '%Y-%m-%d %H:%i:%s') as 'stempelczasowy';";

    /**
     * Nazwy atrybutów zwracanych przez zapytanie numer 2.
     */
    private enum Query2Enum {
        FIRST_TOP_UP,   /**< Data pierwszego doładowania. */
        LAST_TOP_UP     /**< Data ostatniego doładowania. */
    };
    private EnumMap<Query2Enum, String> query2Map;          /**< Wyniki zapytania nr. 2. */
    private final String QUERY_2_FIRST_LAST_TOP_UP =        /**< Treść zapytania nr. 2. */
            "select DATE_FORMAT(min(stempelczasowy), '%Y-%m-%d %H:%i:%s') as 'zakup_biletu', DATE_FORMAT(max(stempelczasowy), '%Y-%m-%d %H:%i:%s') as 'ost_dolad'\n" +
            "from hist_dolad h where h.karnet_id = ? group by h.karnet_id;";

    /**
     * Nazwy atrybutów zwracanych przez zapytanie numer 3.
     */
    private enum Query3Enum {
        PRICE_LIST_POSITION_ID,     /**< Numer id pozycji cennika. */
        PRICE_LIST_POSITION_NAME,   /**< Nazwa pozycji cennika. */
        UNIT_PRICE,                 /**< Cena jednostkowa. */
        TOP_UPS_NUMBER,             /**< Liczba doładowań. */
        POINTS_COUNT,               /**< Suma zakupionych punktów. */
        AMOUNT                      /**< Wydana kwota pieniędzy. */
    };
    private ArrayList<EnumMap<Query3Enum, String>> query3ListOfMaps;    /**< Wyniki zapytania nr. 3. */
    private final String QUERY_3_TOP_UPS_BY_PRICE_LIST_POSITION =       /**< Treść zapytania nr. 3. */
            "select h.poz_cennik_id, sc.nazwa, c.cena as 'cena_jedn', count(*) as 'l_dolad', sum(h.l_pkt) as 'suma_pkt', sum(c.cena * h.l_pkt) as 'kwota'\n" +
            "from hist_dolad h join poz_cennik c on h.poz_cennik_id = c.id join slownik_cennik sc on c.slownik_cennik_id = sc.id\n" +
            "where h.karnet_id = ? group by h.poz_cennik_id, sc.nazwa, c.cena;";

    /**
     * Nazwy atrybutów zwracanych przez zapytanie numer 4.
     */
    private enum Query4Enum {
        TICKET_BALANCE          /**< Stan punktów biletu. */
    };
    private EnumMap<Query4Enum, String> query4Map;          /**< Wyniki zapytania nr. 4. */
    private final String QUERY_4_TICKET_BALANCE =           /**< Treść zapytania nr. 4. */
            "select (case when kupione is null then 0 else kupione end) - (case when wydane is null then 0 else wydane end) as l_pkt\n" +
                    "from ( select ( select sum(hd.l_pkt) from hist_dolad hd where hd.karnet_id = k.id) as kupione,\n" +
                    "(select sum(wd.koszt_pkt) from uzycia_karnetu uk join wyciag_dane wd on uk.wyciag_dane_id = wd.id where uk.karnet_id = k.id) as wydane\n" +
            "from karnet k where k.id = ? ) as tmp;";

    /**
     * Nazwy atrybutów zwracanych przez zapytanie numer 5.
     */
    private enum Query5Enum {
        TOTAL_USE_COUNT,        /**< Całkowita liczba użyć biletu. */
        TOTAL_POINTS_SPENT,     /**< Całkowita libcza wydanych punktów. */
        TOTAL_HEIGHT            /**< Całkowite przewyższenie. */
    };
    private EnumMap<Query5Enum, String> query5Map;                      /**< Wyniki zapytania nr. 5. */
    private final String QUERY_5_TOTAL_USE_COUNT_POINTS_SPEND_HEIGHT =  /**< Treść zapytania nr. 5. */
            "select count(uk.id) as 'l_uzyc', sum(wd.koszt_pkt) as 'wydane_pkt', sum(w.wysokosc) as 'przewyz_calk'\n" +
            "from uzycia_karnetu uk join wyciag_dane wd on uk.wyciag_dane_id = wd.id join wyciag w on wd.wyciag_id = w.id\n" +
            "where uk.karnet_id = ? group by uk.karnet_id;";

    /**
     * Nazwy atrybutów zwracanych przez zapytanie numer 6.
     */
    private enum Query6Enum {
        SKI_LIFT_ID,                /**< Numer id wyciągu. */
        SKI_LIFT_NAME,              /**< Nazwa wyciągu. */
        USE_COUNT_SINGLE_LIFT,      /**< Libcza użyć wyciągu. */
        POINTS_SPENT_SINGLE_LIFT,   /**< Punkty wydane na wyciągu. */
        HEIGHT_SINGLE_LIFT          /**< Całkowite przewyższenie na wyciągu. */
    };
    private ArrayList<EnumMap<Query6Enum, String>> query6ListOfMaps;            /**< Wyniki zapytania nr. 6. */
    private final String QUERY_6_USE_COUNT_POINTS_SPEND_HEIGHT_BY_SKI_LIFT =    /**< Treść zapytania nr. 6. */
            "select w.id, w.nazwa, count(*) as 'l_zjad', sum(wd.koszt_pkt) as 'wydane_pkt', sum(w.wysokosc) as 'przewyz'\n" +
            "from uzycia_karnetu uk join wyciag_dane wd on uk.wyciag_dane_id = wd.id join wyciag w on wd.wyciag_id = w.id\n" +
            "where uk.karnet_id = ? group by w.id, w.nazwa order by sum(w.wysokosc) desc;";

    /**
     * Domyślny konstruktor.
     * @param user Obiekt użytkownika systemu.
     */
    public TicketUseReport(SystemUser user) {
        systemUser = user;
    }

    /**
     * Getter.
     * @return Zwraca raport sformatowany zgodnie z szablonem HTML.
     */
    @Override
    public String getHtmlReport() {
        return htmlReport;
    }

    /**
     * Getter.
     * @return Zwraca opis ostatniego błędu.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Metoda odpowiedzialna za generację raportu.
     * @param ticketId Numer id biletu.
     * @return Zwraca true jeżeli operacja zakończyła się pomyślnie.
     */
    public boolean generateReport(Integer ticketId) {

        this.ticketId = ticketId;

        if(!isExistingId()) return false;
        if(!fetchAllData()) return false;

        try {
            htmlReport = Files.readString(Paths.get(HTML_TEMPLATE_PATH));
        }
        catch (IOException ex) {
            lastError = ex.getMessage();
            return false;
        }

        htmlReport = htmlReport.replace("$gen_rep_timestamp", query1Map.get(Query1Enum.TIMESTAMP));
        htmlReport = htmlReport.replace("$supervisor_name_surname", (systemUser.getName() + " " + systemUser.getSurname()));

        htmlReport = htmlReport.replace("$ticket_no", ticketId.toString());
        htmlReport = htmlReport.replace("$first_top_up", query2Map.get(Query2Enum.FIRST_TOP_UP));
        htmlReport = htmlReport.replace("$last_top_up", query2Map.get(Query2Enum.LAST_TOP_UP));

        htmlReport = htmlReport.replace("$ticket_balance", query4Map.get(Query4Enum.TICKET_BALANCE));
        htmlReport = htmlReport.replace("$use_count", query5Map.get(Query5Enum.TOTAL_USE_COUNT));
        htmlReport = htmlReport.replace("$total_points_spent", query5Map.get(Query5Enum.TOTAL_POINTS_SPENT));
        htmlReport = htmlReport.replace("$total_height", query5Map.get(Query5Enum.TOTAL_HEIGHT));

        StringBuilder sb1 = new StringBuilder("");
        query3ListOfMaps
                .stream()
                .map(query3 -> (
                        "<tr><td>" + query3.get(Query3Enum.PRICE_LIST_POSITION_ID) + "</td>" +
                        "<td>" + query3.get(Query3Enum.PRICE_LIST_POSITION_NAME) + "</td>" +
                        "<td>" + query3.get(Query3Enum.UNIT_PRICE) + "</td>" +
                        "<td>" + query3.get(Query3Enum.TOP_UPS_NUMBER) + "</td>" +
                        "<td>" + query3.get(Query3Enum.POINTS_COUNT) + "</td>" +
                        "<td>" + query3.get(Query3Enum.AMOUNT) + "</td></tr>"

                ))
                .forEach(sb1::append);
        htmlReport = htmlReport.replace("$table_1_content", sb1.toString());

        StringBuilder sb2 = new StringBuilder("");
        query6ListOfMaps
                .stream()
                .map(query6 -> (
                        "<tr><td>" + query6.get(Query6Enum.SKI_LIFT_NAME) + "</td>" +
                        "<td>" + query6.get(Query6Enum.USE_COUNT_SINGLE_LIFT) + "</td>" +
                        "<td>" + query6.get(Query6Enum.POINTS_SPENT_SINGLE_LIFT) + "</td>" +
                        "<td>" + query6.get(Query6Enum.HEIGHT_SINGLE_LIFT) + "</td></tr>"
                 ))
                .forEach(sb2::append);
        htmlReport = htmlReport.replace("$table_2_content", sb2.toString());

        // Wykres
        String chartFileName = "TicketUseChart";
        ArrayList<String> xData = new ArrayList<>();
        //ArrayList<Integer> yData1 = new ArrayList<>();
        ArrayList<Double> yData2 = new ArrayList<>();

        query6ListOfMaps.stream().map(i -> i.get(Query6Enum.SKI_LIFT_NAME)).forEach(xData::add);
        //query6ListOfMaps.stream().map(i -> Integer.parseInt(i.get(Query6Enum.POINTS_SPENT_SINGLE_LIFT))).forEach(yData1::add);
        query6ListOfMaps.stream().map(i -> Double.parseDouble(i.get(Query6Enum.HEIGHT_SINGLE_LIFT))).forEach(yData2::add);

        ReportChart reportChart = new ReportChart("\"Wykres przewyższeń z podziałem na wyciągi.", "Nazwa wyciągu", "");
        //reportChart.addSeries("Wydane punkty", xData, yData1);
        reportChart.addSeries("Przewyższenie", xData, yData2);
        if(!reportChart.saveAs(chartFileName)) {
            lastError = reportChart.getLastError();
            return false;
        };
        htmlReport = htmlReport.replace("$chart_file_name", chartFileName);

        return true;
    }

    /**
     * Metoda walidująca czy numer id biletu istnieje w bazie.
     * @return Zwraca true jeżeli istnieje bilet o podanym numerze id.
     */
    private boolean isExistingId() {
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }
        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement("select * from karnet where id=?;");
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();
            if(rs.first()) {
                return true;
            }
            else {
                lastError = "Karnet o numerze " + ticketId + " nie istnieje.";
            }
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    /**
     * Metoda odpowiedzialna za pobranie wszystkich danych potrzebych do raportu z bazy.
     * @return Zwraca true jeżeli operacja zakończyła się pomyślnie.
     */
    private boolean fetchAllData() {

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        Connection connection = MySQLConnection.getConnection();

        try {
            PreparedStatement ps1 = connection.prepareStatement(QUERY_1_GENERATION_TIME_STAMP);
            ResultSet rs1 = ps1.executeQuery();
            query1Map = new EnumMap<>(Query1Enum.class);
            if(rs1.first()) {
                query1Map.put(Query1Enum.TIMESTAMP, rs1.getString(1));
            }

            PreparedStatement ps2 = connection.prepareStatement(QUERY_2_FIRST_LAST_TOP_UP);
            ps2.setInt(ID_PARAM_POSITION, ticketId);
            ResultSet rs2 = ps2.executeQuery();
            query2Map = new EnumMap<>(Query2Enum.class);
            if(rs2.first()) {
                query2Map.put(Query2Enum.FIRST_TOP_UP, rs2.getString(1));
                query2Map.put(Query2Enum.LAST_TOP_UP, rs2.getString(2));
            }

            PreparedStatement ps3 = connection.prepareStatement(QUERY_3_TOP_UPS_BY_PRICE_LIST_POSITION);
            ps3.setInt(ID_PARAM_POSITION, ticketId);
            ResultSet rs3 = ps3.executeQuery();
            query3ListOfMaps = new ArrayList<>();
            while (rs3.next()) {
                EnumMap rs3Map = new EnumMap<>(Query3Enum.class);
                rs3Map.put(Query3Enum.PRICE_LIST_POSITION_ID, rs3.getString(1));
                rs3Map.put(Query3Enum.PRICE_LIST_POSITION_NAME, rs3.getString(2));
                rs3Map.put(Query3Enum.UNIT_PRICE, rs3.getString(3));
                rs3Map.put(Query3Enum.TOP_UPS_NUMBER, rs3.getString(4));
                rs3Map.put(Query3Enum.POINTS_COUNT, rs3.getString(5));
                rs3Map.put(Query3Enum.AMOUNT, rs3.getString(6));
                query3ListOfMaps.add(rs3Map);
            }

            PreparedStatement ps4 = connection.prepareStatement(QUERY_4_TICKET_BALANCE);
            ps4.setInt(ID_PARAM_POSITION, ticketId);
            ResultSet rs4 = ps4.executeQuery();
            query4Map = new EnumMap<>(Query4Enum.class);
            if(rs4.first()) {
                query4Map.put(Query4Enum.TICKET_BALANCE, rs4.getString(1));
            }

            PreparedStatement ps5 = connection.prepareStatement(QUERY_5_TOTAL_USE_COUNT_POINTS_SPEND_HEIGHT);
            ps5.setInt(ID_PARAM_POSITION, ticketId);
            ResultSet rs5 = ps5.executeQuery();
            query5Map = new EnumMap<>(Query5Enum.class);
            if(rs5.first()) {
                query5Map.put(Query5Enum.TOTAL_USE_COUNT, rs5.getString(1));
                query5Map.put(Query5Enum.TOTAL_POINTS_SPENT, rs5.getString(2));
                query5Map.put(Query5Enum.TOTAL_HEIGHT, rs5.getString(3));
            }

            PreparedStatement ps6 = connection.prepareStatement(QUERY_6_USE_COUNT_POINTS_SPEND_HEIGHT_BY_SKI_LIFT);
            ps6.setInt(ID_PARAM_POSITION, ticketId);
            ResultSet rs6 = ps6.executeQuery();
            query6ListOfMaps = new ArrayList<>();
            while (rs6.next()) {
                EnumMap rs6Map = new EnumMap<>(Query6Enum.class);
                rs6Map.put(Query6Enum.SKI_LIFT_ID, rs6.getString(1));
                rs6Map.put(Query6Enum.SKI_LIFT_NAME, rs6.getString(2));
                rs6Map.put(Query6Enum.USE_COUNT_SINGLE_LIFT, rs6.getString(3));
                rs6Map.put(Query6Enum.POINTS_SPENT_SINGLE_LIFT, rs6.getString(4));
                rs6Map.put(Query6Enum.HEIGHT_SINGLE_LIFT, rs6.getString(5));
                query6ListOfMaps.add(rs6Map);
            }

            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }
}
