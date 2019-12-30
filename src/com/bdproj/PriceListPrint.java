package com.bdproj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Klasa generująca wydruk cennika sformatowanego zgodnie z szablonem HTML.
 * @see HtmlReport
 */
public class PriceListPrint implements HtmlReport {

    private final String HTML_TEMPLATE_PATH = "reports/templates/PriceListTable.html";  /**< Ścieżka do pliku szablonu cennika. */
    private final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";                      /**< Format stemplaczasowego. */
    private String htmlPriceList;   /**< String zawierający cennik sformatowany zgodnie z szablonem HTML. */
    private String lastError;       /**< Opis ostatniego błędu. */

    private String validSince;      /**< Data od kiedy ważny jest cennik. */
    private String validTo;         /**< Data do kiedy ważny jest cennik. */
    /**
     * Lista zawierająca pozycje cennika.
     */
    private ArrayList<EnumMap<PriceList.PriceListEnum, String>> priceListItems;

    /**
     * Domyślny konstruktor.
     * @param validSince Data od kiedy ważny jest cennik.
     * @param validTo Data do kiedy ważny jest cennik.
     * @param priceListItems Lista zawierająca pozycje cennika.
     */
    PriceListPrint(String validSince, String validTo, ArrayList<EnumMap<PriceList.PriceListEnum, String>> priceListItems) {
        this.validSince = validSince;
        this.validTo = validTo;
        this.priceListItems = priceListItems;
    }

    /**
     * Metoda wypełniająca szablon cennika.
     * @return Zwraca true jeżeli operacja zakończyła się sukcesem.
     */
    public boolean generatePriceListHtml() {
        try {
            htmlPriceList = Files.readString(Paths.get(HTML_TEMPLATE_PATH));
        }
        catch (IOException ex) {
            lastError = ex.getMessage();
            return false;
        }

        String generationTimestamp;
        try {
            generationTimestamp = (new SimpleDateFormat(TIMESTAMP_FORMAT)).format(MySQLConnection.getServerTimestamp());
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
            return false;
        }

        htmlPriceList = htmlPriceList.replace("$timestamp", generationTimestamp);
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

    /**
     * Getter.
     * @return Zwraca opis ostatniego błędu.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Getter.
     * @return Zwraca cennik sformatowany zgodnie z szablonem HTML.
     * @see generatePriceListHtml()
     */
    @Override
    public String getHtmlReport() {
        return htmlPriceList;
    }
}
