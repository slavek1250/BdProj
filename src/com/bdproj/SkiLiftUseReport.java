package com.bdproj;

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
 * Klasa służąca do generacji raportu użyć wyciągów.
 * Raport zawiera:
 *
 *                                                                  Data generacji raportu: yyyy-MM-dd hh:mm:ss
 *                                                                        Wygenerowano dla: Imie Nazwisko
 *
 *
 *                                                    Raport użyć wyciągu
 *
 *      Nazwa wyciągu: nazwa.
 *      Numer id wyciągu: id.
 *      Początek okresu zawartego w raporcie: yyyy-MM-dd hh:mm:ss
 *      Koniec raportowanego okresu: yyyy-MM-dd hh:mm:ss
 *
 *      W tym okresie skorzystano z wyciągu: n raz[y].
 *      Wyciąg zarobił: m pkt.
 *      Szacowana kwota: o zł.
 *
 *      Czas    |   Liczba użyć     |   Liczba punktów     |   Szacowana kwota
 *
 * @see HtmlReport
 */
public class SkiLiftUseReport implements HtmlReport {

    /**
     * Format daty.
     */
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Generowany raport zawiera dane grupowane względem podstaw czasowych wybieranych zależnie od okresu jakiego dotyczy raport.
     */
    enum TimeBaseEnum {
        HOUR,   /**< Grupowanie względem godzin jeżeli raport dotyczy konkretnego dnia. */
        DAY,    /**< Grupowanie względem dni jeżeli raportowany okres jest krótszy niż 30 dni. */
        MONTH,  /**< Grupowanie względem miesięcy jeżeli raportowany okres jest dłuższy niż 30 dni i krótszy niż rok. */
        YEAR    /**< Grupowanie względem lat jeżeli raportowany okres jest dłuższy niż 365 dni. */
    };
    EnumMap<TimeBaseEnum, String> timeBaseNames;    /**< Nazwy podstaw czasowych. */
    EnumMap<TimeBaseEnum, String> timeBaseGroupBy;  /**< Wartości podstaw czasowych do grupowania w zapytaniu SQL. */

    private final long MORE_THAN_A_DAY = 24 * 60 * 60 * 1000;       /**< Liczba milisekund w jednym dniu. */
    private final long MORE_THAN_A_MONTH = MORE_THAN_A_DAY * 30;    /**< Liczba milisekund w jednym miesiącu. */
    private final long MORE_THAN_AN_YEAR = MORE_THAN_A_DAY * 365;   /**< Liczba milisekund w jednym roku. */

    /**
     * Ścieżka do szblonu raportu w HTML.
     */
    private final String HTML_TEMPLATE_PATH = "reports/templates/SkiLiftUseTemplate.html";

    /**
     * Dane zwracane przez zapytanie pierwsze.
     */
    private enum Query1Enum {
        TIMESTAMP   /**< Stempelczasowy generacji raportu. */
    };
    private EnumMap<Query1Enum, String> query1Map;                              /**< Wyniki zapytania pierwszego. */
    private final String QUERY_1_TIMESTAMP =                                    /**< Treść zapytania pierwszego. */
            "select DATE_FORMAT(now(), '%Y-%m-%d %H:%i:%s') as 'stempelczasowy';";

    /**
     * Dane zwracane przez zapytanie drugie.
     */
    private enum Query2Enum {
        TOTAL_USE_COUNT,        /**< Całkowita liczba użyć wyciągu. */
        TOTAL_POINTS_SPENT,     /**< Całkowita liczba wydanych punktów. */
        TOTAL_AMOUNT            /**< Całkowita szacowana kwota zarobionych pieniędzy. */
    };
    private EnumMap<Query2Enum, String> query2Map;                              /**< Wyniki zapytania drugiego. */
    private final String QUERY_2_TOTAL_USE_COUNT_POINTS_SPENT_AMOUNT =          /**< Treść zapytania drugiego. */
            "select count(*) as l_uzyc, sum(wd.koszt_pkt) as wydano_pkt, round((sum(wd.koszt_pkt) * (select sum(pc.cena * hd.l_pkt) / sum(hd.l_pkt) from hist_dolad hd join poz_cennik pc on hd.poz_cennik_id = pc.id)), 2) as kwota\n" +
            "from wyciag_dane wd join uzycia_karnetu uk on wd.id = uk.wyciag_dane_id\n" +
            "where wd.wyciag_id = ? and uk.stempelczasowy >= ? and uk.stempelczasowy < ?;";

    /**
     * Dane zwracane przez zapytanie trzecie.
     */
    private enum Query3Enum {
        TIME,                   /**< Czas zgrupowanych danych. */
        USE_COUNT,              /**< Liczba użyć. */
        POINTS_SPENT,           /**< Liczba wydanych punktów. */
        AMOUNT                  /**< Szacowana zarobiona kwota. */
    }
    private ArrayList<EnumMap<Query3Enum, String>> query3ListOfMaps;            /**< Wyniki zapytania trzeciego. */
    private String query3GroupBy = null;                                        /**< Podstawa czasowa do grupowania danych. */
    private final String QUERY_3_USE_COUNT_POINTS_SPENT_AMOUNT_BY_SKI_LIFT =    /**< Treść zapytania trzeciego. */
            "select DATE_FORMAT(uk.stempelczasowy, ?) as okres,\n" +
            "count(*) as l_uzyc, sum(wd.koszt_pkt) as wydano_pkt, round((sum(wd.koszt_pkt) * (select sum(pc.cena * hd.l_pkt) / sum(hd.l_pkt) from hist_dolad hd join poz_cennik pc on hd.poz_cennik_id = pc.id)), 2) as kwota\n" +
            "from wyciag_dane wd join uzycia_karnetu uk on wd.id = uk.wyciag_dane_id\n" +
            "where wd.wyciag_id = ? and uk.stempelczasowy >= ? and uk.stempelczasowy < ? group by okres order by okres;";

    private Integer skiLiftId;      /**< Numer id wyciągu. */
    private String timeBegin;       /**< Data początku raportowanego okresu. */
    private String timeEnd;         /**< Data końca raportowanego okersu. */

    private SystemUser systemUser;  /**< Obiekt obecnie zalogowanego użytkownika systemu. */
    private String htmlReport = null;   /**< Raport sformatowany w HTML. */
    private String lastError;           /**< Opis ostatniego błędu. */

    /**
     * Domyślny konstruktor.
     * @param user Obiekt obecnie zalogowanego użytkownika.
     */
    SkiLiftUseReport(SystemUser user) {
        systemUser = user;
        timeBaseNames = new EnumMap<>(TimeBaseEnum.class);
        timeBaseGroupBy = new EnumMap<>(TimeBaseEnum.class);
        timeBaseNames.put(TimeBaseEnum.HOUR, "Godzina");
        timeBaseGroupBy.put(TimeBaseEnum.HOUR, "%H");
        timeBaseNames.put(TimeBaseEnum.DAY, "Dzień");
        timeBaseGroupBy.put(TimeBaseEnum.DAY, "%Y-%m-%d");
        timeBaseNames.put(TimeBaseEnum.MONTH, "Miesiąc");
        timeBaseGroupBy.put(TimeBaseEnum.MONTH, "%Y-%m");
        timeBaseNames.put(TimeBaseEnum.YEAR, "Rok");
        timeBaseGroupBy.put(TimeBaseEnum.YEAR, "%Y");
    }

    /**
     * Getter.
     * @return Zwraca wygenerowany raport sformatowany w HTML.
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
     * Metoda generująca raport w HTML.
     * @param skiLiftId Numer id wyciągu.
     * @param skiLiftName Nazwa wyciągu.
     * @param begin Data początku raportowanego okresu.
     * @param end Data końca raportowanego okersu.
     * @return Zwraca true jeżeli operacja zakończyła się sukcesem.
     */
    public boolean generateReport(Integer skiLiftId, String skiLiftName, Date begin, Date end) {

        TimeBaseEnum currentTimeBase = getTimeUnitToGroupBy(begin, end);
        query3GroupBy = timeBaseGroupBy.get(currentTimeBase);
        timeBegin = DATE_FORMAT.format(begin);
        timeEnd = DATE_FORMAT.format(end);
        this.skiLiftId = skiLiftId;

        if(!fetchAllData()) return false;

        if(query2Map.get(Query2Enum.TOTAL_USE_COUNT).equals("0")) {
            lastError = "Niestety, w tym okresie wyciąg był nieużywany, nie można wygenerować raportu. Przepraszamy.";
            return false;
        }

        try {
            //htmlReport = Files.readString(Paths.get(HTML_TEMPLATE_PATH));
            htmlReport = new String(Files.readAllBytes(Paths.get(HTML_TEMPLATE_PATH)));
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

        htmlReport = htmlReport.replace("$time_base", timeBaseNames.get(currentTimeBase));
        htmlReport = htmlReport.replace("$table_1_content", sb.toString());

        // wykres
        String chartFileName = "SkiLiftUseChart";
        ArrayList<String> xData = new ArrayList<>();
        ArrayList<Integer> yData1 = new ArrayList<>();
        ArrayList<Double> yData2 = new ArrayList<>();

        query3ListOfMaps.stream().map(i -> i.get(Query3Enum.TIME)).forEach(xData::add);
        query3ListOfMaps.stream().map(i -> Integer.parseInt(i.get(Query3Enum.POINTS_SPENT))).forEach(yData1::add);
        query3ListOfMaps.stream().map(i -> Double.parseDouble(i.get(Query3Enum.AMOUNT))).forEach(yData2::add);

        ReportChart reportChart = new ReportChart("Wykres punktów, oraz przybliżonych kwot wydanych na bieżącym wyciągu.", timeBaseNames.get(currentTimeBase), "");
        reportChart.addSeries("Punkty", xData, yData1);
        reportChart.addSeries("Kwota [zł]", xData, yData2);
        if(!reportChart.saveAs(chartFileName)) {
            lastError = reportChart.getLastError();
            return false;
        };
        htmlReport = htmlReport.replace("$chart_file_name", chartFileName);

        return true;
    }

    /**
     * Metoda pobierająca z bazy danych wszystkie dane potrzebne do stworzenia raportu.
     * @return Zwraca true jeżeli operacja zakończyła się sukcesem.
     */
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

    /**
     * Metoda ustalająca podstawę czasową raportu.
     * @param begin Data początku raportowanego okresu.
     * @param end Data końca raportowanego okersu.
     * @return Numer id podstawy czasowej.
     */
    private TimeBaseEnum getTimeUnitToGroupBy(Date begin, Date end) {
        long difference = end.getTime() - begin.getTime();
        if(difference > MORE_THAN_AN_YEAR) {
            return TimeBaseEnum.YEAR;
        }
        else if (difference > MORE_THAN_A_MONTH) {
            return TimeBaseEnum.MONTH;
        }
        else if(difference > MORE_THAN_A_DAY) {
            return TimeBaseEnum.DAY;
        }
        return TimeBaseEnum.HOUR;
    }
}
