package com.bdproj;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.util.ArrayList;

// https://knowm.org/open-source/xchart/xchart-example-code/
public class ReportChart {

    private final String PATH_TO_SAVE = "reports/";
    private String lastError;
    private CategoryChart chart;

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

    public void addSeries(String seriesName, ArrayList<?> xData, ArrayList<? extends Number> yData) {
        chart.addSeries(seriesName, xData, yData);
    }

    public String getLastError(){
        return lastError;
    }

    public CategoryChart getChart() {
        return chart;
    }
}
