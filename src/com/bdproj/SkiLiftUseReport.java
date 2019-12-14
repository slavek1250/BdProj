package com.bdproj;

import javafx.util.Pair;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;

/**
 *                                                                  Data generacji raportu: yyyy-MM-dd hh:mm:ss
 *                                                                        Wygenerowano dla: Imie Nazwisko
 *
 *
 *                                                    Raport użyć biletu
 *
 *      Nazwa wyciągu: nazwa.
 *      Numer id wyciągu: id.
 *      Początek okresu zawartego w raporcie: yyyy-MM-dd hh:mm:ss [początek istnienia]
 *      Koniec raportowanego okresu: yyyy-MM-dd hh:mm:ss
 *
 *      W tym okresie skorzystano z wyciągu: n raz[y].
 *      Wyciąg zarobił: n pkt.
 *      Szacowana kwota: m zł.
 *
 *      Czas    |   Liczba użyć     |   Liczba punktów     |   Szacowana kwota
 *
 *
 */

public class SkiLiftUseReport implements HtmlReport {

    private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final Pair<String, String> GROUP_BY_HOUR = new Pair<>("%H", "Godzina");
    private final Pair<String, String> GROUP_BY_DAY = new Pair<>("%Y-%m-%d", "Dzień");
    private final Pair<String, String> GROUP_BY_MONTH = new Pair<>("%Y-%m", "Miesiąc");
    private final Pair<String, String> GROUP_BY_YEAR = new Pair<>("%Y", "Rok");

    // by hour
    private final long MORE_THAN_A_DAY = 24 * 60 * 60 * 1000; // by day
    private final long MORE_THAN_A_MONTH = MORE_THAN_A_DAY * 31; // by month
    private final long MORE_THAN_AN_YEAR = MORE_THAN_A_DAY * 365; // by year

    private final String HTML_TEMPLATE_PATH = "reports/templates/SkiLiftUseTemplate.html";

    private enum Query1Enum { TIMESTAMP };
    private EnumMap<Query1Enum, String> query1Map;
    private final String QUERY_1_TIMESTAMP =
            "select DATE_FORMAT(now(), '%Y-%m-%d %H:%i:%s') as 'stempelczasowy';";

    private enum Query2Enum { TOTAL_USE_COUNT, TOTAL_POINTS_SPENT, TOTAL_AMOUNT };
    private EnumMap<Query2Enum, String> query2Map;
    private final String QUERY_2_TOTAL_USE_COUNT_POINTS_SPENT_AMOUNT =
            "select count(*) as l_uzyc, sum(wd.koszt_pkt) as wydano_pkt, (sum(wd.koszt_pkt) * (select sum(pc.cena * hd.l_pkt) / sum(hd.l_pkt) from hist_dolad hd join poz_cennik pc on hd.poz_cennik_id = pc.id)) as kwota\n" +
            "from wyciag_dane wd join uzycia_karnetu uk on wd.id = uk.wyciag_dane_id\n" +
            "where wd.wyciag_id = ? and uk.stempelczasowy >= ? and uk.stempelczasowy < ?;";

    private String query3GroupBy = null;
    private enum Query3Enum { TIME, USE_COUNT, POINTS_SPENT, AMOUNT }
    private ArrayList<EnumMap<Query3Enum, String>> query3ListOfMaps;
    private final String QUERY_3_USE_COUNT_POINTS_SPENT_AMOUNT_BY_SKI_LIFT =
            "select DATE_FORMAT(uk.stempelczasowy, ?) as okres,\n" +
            "count(*) as l_uzyc, sum(wd.koszt_pkt) as wydano_pkt, (sum(wd.koszt_pkt) * (select sum(pc.cena * hd.l_pkt) / sum(hd.l_pkt) from hist_dolad hd join poz_cennik pc on hd.poz_cennik_id = pc.id)) as kwota\n" +
            "from wyciag_dane wd join uzycia_karnetu uk on wd.id = uk.wyciag_dane_id\n" +
            "where wd.wyciag_id = ? and uk.stempelczasowy >= ? and uk.stempelczasowy < ? group by okres order by okres;";

    private Integer skiLiftId;
    private String timeBegin;
    private String timeEnd;

    private SystemUser systemUser;
    private String htmlReport = null;
    private String lastError;

    SkiLiftUseReport(SystemUser user) {
        systemUser = user;
    }

    @Override
    public String getHtmlReport() {
        return htmlReport;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean generateReport(Integer skiLiftId, String skiLiftName, Date begin, Date end) {

        Pair<String, String> currentTimeBase = getTimeUnitToGroupBy(begin, end);
        query3GroupBy = currentTimeBase.getKey();
        timeBegin = DATE_FORMAT.format(begin);
        timeEnd = DATE_FORMAT.format(end);
        this.skiLiftId = skiLiftId;

        if(!fetchAllData()) return false;

        if(query2Map.get(Query2Enum.TOTAL_USE_COUNT).equals("0")) {
            lastError = "Niestety, w tym okresie wyciąg był nieużywany, nie można wygenerować raportu. Przepraszamy.";
            return false;
        }

        try {
            htmlReport = Files.readString(Paths.get(HTML_TEMPLATE_PATH));
        }
        catch (IOException ex) {
            lastError = ex.getMessage();
            return false;
        }

        htmlReport = htmlReport.replace("$gen_rep_timestamp", query1Map.get(Query1Enum.TIMESTAMP));
        htmlReport = htmlReport.replace("$supervisor_name_surname", (systemUser.getName() + " " + systemUser.getSurname()));

        htmlReport = htmlReport.replace("$ski_lift_name", skiLiftName);
        htmlReport = htmlReport.replace("$ski_lift_id", skiLiftId.toString());

        htmlReport = htmlReport.replace("$rep_begin_date", timeBegin);
        htmlReport = htmlReport.replace("$rep_end_date", timeEnd);

        htmlReport = htmlReport.replace("$total_use_count", query2Map.get(Query2Enum.TOTAL_USE_COUNT));
        htmlReport = htmlReport.replace("$total_points_spent", query2Map.get(Query2Enum.TOTAL_POINTS_SPENT));
        htmlReport = htmlReport.replace("$total_amount", query2Map.get(Query2Enum.TOTAL_AMOUNT));

        StringBuilder sb = new StringBuilder("");
        query3ListOfMaps
                .stream()
                .map(single -> (
                        "<tr><td>" + single.get(Query3Enum.TIME) + "</td>" +
                        "<td>" + single.get(Query3Enum.USE_COUNT) + "</td>" +
                        "<td>" + single.get(Query3Enum.POINTS_SPENT) + "</td>" +
                        "<td>" + single.get(Query3Enum.AMOUNT) + "</td></tr>"
                ))
                .forEach(sb::append);

        htmlReport = htmlReport.replace("$time_base", currentTimeBase.getValue());
        htmlReport = htmlReport.replace("$table_1_content", sb.toString());


        // wykres
        ArrayList<String> xData = new ArrayList<>();
        ArrayList<Integer> yData = new ArrayList<>();

        query3ListOfMaps.stream().map(i -> i.get(Query3Enum.TIME)).forEach(xData::add);
        query3ListOfMaps.stream().map(i -> Integer.parseInt(i.get(Query3Enum.POINTS_SPENT))).forEach(yData::add);

        CategoryChart chart = new CategoryChartBuilder().width(550).height(400).title("Liczba punktów").xAxisTitle(currentTimeBase.getValue()).yAxisTitle("Punkty").theme(Styler.ChartTheme.GGPlot2).build();
        chart.addSeries("Punkty", xData, yData);

        try {
            BitmapEncoder.saveBitmap(chart, "reports/chart", BitmapEncoder.BitmapFormat.JPG);
        }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        //new SwingWrapper(chart).displayChart();


        return true;
    }


    private boolean fetchAllData() {

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }
        Connection connection = MySQLConnection.getConnection();

        try {
            PreparedStatement ps1 = connection.prepareStatement(QUERY_1_TIMESTAMP);
            ResultSet rs1 = ps1.executeQuery();
            query1Map = new EnumMap<>(Query1Enum.class);
            if(rs1.first()) {
                query1Map.put(Query1Enum.TIMESTAMP, rs1.getString(1));
            }

            PreparedStatement ps2 = connection.prepareStatement(QUERY_2_TOTAL_USE_COUNT_POINTS_SPENT_AMOUNT);
            ps2.setInt(1, skiLiftId);
            ps2.setString(2, timeBegin);
            ps2.setString(3, timeEnd);
            ResultSet rs2 = ps2.executeQuery();
            query2Map = new EnumMap<>(Query2Enum.class);
            if(rs2.first()) {
                query2Map.put(Query2Enum.TOTAL_USE_COUNT, rs2.getString(1));
                query2Map.put(Query2Enum.TOTAL_POINTS_SPENT, rs2.getString(2));
                query2Map.put(Query2Enum.TOTAL_AMOUNT, rs2.getString(3));
            }

            PreparedStatement ps3 = connection.prepareStatement(QUERY_3_USE_COUNT_POINTS_SPENT_AMOUNT_BY_SKI_LIFT);
            ps3.setString(1, query3GroupBy);
            ps3.setInt(2, skiLiftId);
            ps3.setString(3, timeBegin);
            ps3.setString(4, timeEnd);
            ResultSet rs3 = ps3.executeQuery();
            query3ListOfMaps = new ArrayList<>();
            while (rs3.next()) {
                EnumMap<Query3Enum, String> tmp = new EnumMap<>(Query3Enum.class);
                tmp.put(Query3Enum.TIME, rs3.getString(1));
                tmp.put(Query3Enum.USE_COUNT, rs3.getString(2));
                tmp.put(Query3Enum.POINTS_SPENT, rs3.getString(3));
                tmp.put(Query3Enum.AMOUNT, rs3.getString(4));
                query3ListOfMaps.add(tmp);
            }

            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    private Pair<String, String> getTimeUnitToGroupBy(Date begin, Date end) {
        long difference = end.getTime() - begin.getTime();
        if(difference > MORE_THAN_AN_YEAR) {
            return GROUP_BY_YEAR;
        }
        else if (difference > MORE_THAN_A_MONTH) {
            return GROUP_BY_MONTH;
        }
        else if(difference > MORE_THAN_A_DAY) {
            return GROUP_BY_DAY;
        }
        return GROUP_BY_HOUR;
    }
}
