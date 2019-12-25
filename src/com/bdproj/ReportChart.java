package com.bdproj;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.ChartColor;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

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
/*
        chart.getStyler().setPlotBackgroundColor(ChartColor.getAWTColor(ChartColor.GREY));
        chart.getStyler().setPlotGridLinesColor(new Color(255, 255, 255));
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendBackgroundColor(Color.PINK);
        chart.getStyler().setChartFontColor(Color.MAGENTA);
        chart.getStyler().setChartTitleBoxBackgroundColor(new Color(0, 222, 0));
        chart.getStyler().setChartTitleBoxVisible(true);
        chart.getStyler().setChartTitleBoxBorderColor(Color.BLACK);
        chart.getStyler().setPlotGridLinesVisible(false);

        chart.getStyler().setAxisTickPadding(20);

        chart.getStyler().setAxisTickMarkLength(15);

        chart.getStyler().setPlotMargin(20);

        chart.getStyler().setChartTitleFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        chart.getStyler().setLegendFont(new Font(Font.SERIF, Font.PLAIN, 18));
        chart.getStyler().setLegendSeriesLineLength(12);
        chart.getStyler().setAxisTitleFont(new Font(Font.SANS_SERIF, Font.ITALIC, 18));
        chart.getStyler().setAxisTickLabelsFont(new Font(Font.SERIF, Font.PLAIN, 11));
        chart.getStyler().setDatePattern("dd-MMM");
        chart.getStyler().setDecimalPattern("#0.000");
        chart.getStyler().setLocale(Locale.GERMAN);
*/
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
