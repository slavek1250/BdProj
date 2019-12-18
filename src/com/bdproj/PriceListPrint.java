package com.bdproj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;

public class PriceListPrint implements HtmlReport {

    private final String HTML_TEMPLATE_PATH = "reports/templates/PriceListTable.html";
    private String htmlPriceList;
    private String lastError;

    String validSince;
    String validTo;
    ArrayList<EnumMap<PriceList.PriceListEnum, String>> priceListItems;

    PriceListPrint(String validSince, String validTo, ArrayList<EnumMap<PriceList.PriceListEnum, String>> priceListItems) {
        this.validSince = validSince;
        this.validTo = validTo;
        this.priceListItems = priceListItems;
    }

    public boolean generatePriceListHtml() {
        try {
            htmlPriceList = Files.readString(Paths.get(HTML_TEMPLATE_PATH));
        }
        catch (IOException ex) {
            lastError = ex.getMessage();
            return false;
        }

        htmlPriceList = htmlPriceList.replace("$valid_since", validSince);
        htmlPriceList = htmlPriceList.replace("$valid_to", validTo);

        StringBuilder sb = new StringBuilder("");
        priceListItems
                .stream()
                .map(item -> (
                    "<tr><td>" + item.get(PriceList.PriceListEnum.PRICE_LIST_DICTIONARY_ID) + "</td>" +
                    "<td>" + item.get(PriceList.PriceListEnum.NAME) + "</td>" +
                    "<td>" + item.get(PriceList.PriceListEnum.PRICE) + "</td></tr>"
                ))
                .forEach(sb::append);
        htmlPriceList = htmlPriceList.replace("$table_1_content", sb.toString());
        return true;
    }

    public String getLastError() {
        return lastError;
    }

    @Override
    public String getHtmlReport() {
        return htmlPriceList;
    }
}
