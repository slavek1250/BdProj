package com.bdproj.sys_admin;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.util.ArrayList;

//

/**
 * Klasa odpowiedzialna za generowanie wykresów słupkowych.
 * @see [XChart Example Code](<https://knowm.org/open-source/xchart/xchart-example-code/>)
 */
public class ReportChart {

    private final String PATH_TO_SAVE = "reports/"; /**< Ścieżka katalogu do zapisu wykresu jako plik .png. */
    private String lastError;                       /**< Opis ostatniego błędu. */
    private CategoryChart chart;                    /**< Obiekt wykresu. */

    /**
     * Domyślny konstruktor.
     * @param title Tytuł wykresu.
     * @param xAxisTitle Nazwa osi X.
     * @param yAxisTitle Nazwa osi Y.
     */
    ReportChart(String title, String xAxisTitle, String yAxisTitle) {

        chart = new CategoryChartBuilder()
                .width(550)
                .height(400)
                .title(title)
                .xAxisTitle(xAxisTitle)
                .yAxisTitle(yAxisTitle)
                .theme(Styler.ChartTheme.Matlab)
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setOverlapped(true);
    }

    /**
     * Metoda służąca do zapisu wygenerowanego wykresu jako plik .png.
     * @param filename Nazwa nowo tworzonego pliku.
     * @return Zwraca true jeżeli zakończono operację pomyślnie.
     */
    public boolean saveAs(String filename) {
        try {
            BitmapEncoder.saveBitmapWithDPI(chart, (PATH_TO_SAVE + "/" + filename), BitmapEncoder.BitmapFormat.PNG, 300);
            return true;
        }
        catch (IOException ex) {
            lastError =  ex.getMessage();
        }
        return false;
    }

    /**
     * Metoda dodająca serię danych do wykresu.
     * @param seriesName Nazwa serii danych.
     * @param xData Wartości osi poziomej serii danych.
     * @param yData Wartości osi pionowej serii danych.
     */
    public void addSeries(String seriesName, ArrayList<?> xData, ArrayList<? extends Number> yData) {
        chart.addSeries(seriesName, xData, yData);
    }

    /**
     * Getter.
     * @return Zwraca opis ostatniego błędu.
     */
    public String getLastError(){
        return lastError;
    }

    /**
     * Getter.
     * @return Zwraca obiekt utworzonego wykresu.
     */
    public CategoryChart getChart() {
        return chart;
    }
}
