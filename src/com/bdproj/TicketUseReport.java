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
 *      // sortowane malejąco,
 *      Wykres: wydane punkty, przewyższenie, z podzialem na wyciągi.
 *      Tabelka zawierająca dane z wykresu
 */

public class TicketUseReport implements HtmlReport {

    private String HTML_TEMPLATE_PATH = "reports/templates/TicketUseTemplate.html";

    private SystemUser systemUser;
    private Integer ticketId;
    private String htmlReport = null;
    private String lastError = null;

    private final Integer ID_PARAM_POSITION = 1;

    private enum Query1Enum { TIMESTAMP };
    private EnumMap<Query1Enum, String> query1Map;
    private final String QUERY_1_GENERATION_TIME_STAMP =
            "select DATE_FORMAT(now(), '%Y-%m-%d %H:%i:%s') as 'stempelczasowy';";


    private enum Query2Enum { FIRST_TOP_UP, LAST_TOP_UP };
    private EnumMap<Query2Enum, String> query2Map;
    private final String QUERY_2_FIRST_LAST_TOP_UP =
            "select DATE_FORMAT(min(stempelczasowy), '%Y-%m-%d %H:%i:%s') as 'zakup_biletu', DATE_FORMAT(max(stempelczasowy), '%Y-%m-%d %H:%i:%s') as 'ost_dolad'\n" +
            "from hist_dolad h where h.karnet_id = ? group by h.karnet_id;";

    private enum Query3Enum { PRICE_LIST_POSITION_ID, PRICE_LIST_POSITION_NAME, UNIT_PRICE, TOP_UPS_NUMBER, POINTS_COUNT, AMOUNT };
    private ArrayList<EnumMap<Query3Enum, String>> query3ListOfMaps;
    private final String QUERY_3_TOP_UPS_BY_PRICE_LIST_POSITION =
            "select h.poz_cennik_id, sc.nazwa, c.cena as 'cena_jedn', count(*) as 'l_dolad', sum(h.l_pkt) as 'suma_pkt', sum(c.cena * h.l_pkt) as 'kwota'\n" +
            "from hist_dolad h join poz_cennik c on h.poz_cennik_id = c.id join slownik_cennik sc on c.slownik_cennik_id = sc.id\n" +
            "where h.karnet_id = ? group by h.poz_cennik_id, sc.nazwa, c.cena;";

    private enum Query4Enum { TICKET_BALANCE };
    private EnumMap<Query4Enum, String> query4Map;
    private final String QUERY_4_TICKET_BALANCE =
            "select ( select sum(hd.l_pkt) from hist_dolad hd where hd.karnet_id = k.id)-\n" +
                 "\t(select sum(wd.koszt_pkt) from uzycia_karnetu uk join wyciag_dane wd on uk.wyciag_dane_id = wd.id where uk.karnet_id = k.id) as 'l_pkt'\n" +
            "from karnet k where k.id = ?;";

    private enum Query5Enum { TOTAL_USE_COUNT, TOTAL_POINTS_SPENT, TOTAL_HEIGHT };
    private EnumMap<Query5Enum, String> query5Map;
    private final String QUERY_5_TOTAL_USE_COUNT_POINTS_SPEND_HEIGHT =
            "select count(uk.id) as 'l_uzyc', sum(wd.koszt_pkt) as 'wydane_pkt', sum(w.wysokosc) as 'przewyz_calk'\n" +
            "from uzycia_karnetu uk join wyciag_dane wd on uk.wyciag_dane_id = wd.id join wyciag w on wd.wyciag_id = w.id\n" +
            "where uk.karnet_id = ? group by uk.karnet_id;";

    private enum Query6Enum { SKI_LIFT_ID, SKI_LIFT_NAME, USE_COUNT_SINGLE_LIFT, POINTS_SPEND_SINGLE_LIFT, HEIGHT_SINGLE_LIFT };
    private ArrayList<EnumMap<Query6Enum, String>> query6ListOfMaps;
    private final String QUERY_6_USE_COUNT_POINTS_SPEND_HEIGHT_BY_SKI_LIFT =
            "select w.id, w.nazwa, count(*) as 'l_zjad', sum(wd.koszt_pkt) as 'wydane_pkt', sum(w.wysokosc) as 'przewyz'\n" +
            "from uzycia_karnetu uk join wyciag_dane wd on uk.wyciag_dane_id = wd.id join wyciag w on wd.wyciag_id = w.id\n" +
            "where uk.karnet_id = ? group by w.id, w.nazwa;";


    public TicketUseReport(SystemUser user) {
        systemUser = user;
    }

    @Override
    public String getHtmlReport() {
        return htmlReport;
    }

    public boolean generateReport(Integer ticketId) {

        this.ticketId = ticketId;

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
                        "<td>" + query6.get(Query6Enum.POINTS_SPEND_SINGLE_LIFT) + "</td>" +
                        "<td>" + query6.get(Query6Enum.HEIGHT_SINGLE_LIFT) + "</td></tr>"
                 ))
                .forEach(sb2::append);
        htmlReport = htmlReport.replace("$table_2_content", sb2.toString());

        return true;
    }

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
                rs6Map.put(Query6Enum.POINTS_SPEND_SINGLE_LIFT, rs6.getString(4));
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
