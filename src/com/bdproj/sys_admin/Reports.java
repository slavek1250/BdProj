package com.bdproj.sys_admin;

import java.io.IOException;
import java.util.List;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.font.FontProvider;

/**
 * Klasa zapisująca raporty sformatowane w HTML do pliku .pdf.
 * @see [iText Library Examples](<https://itextpdf.com/en/resources/examples>)
 */
public class Reports {

    private final String FONT_DIR_PATH = "reports/font";    /**< Ścieżka do katalogu zawierającego fonty. */

    private HtmlReport htmlReport;                          /**< Obiekt raportu sformatowanego w HTML. */
    private String lastError;                               /**< Opis ostatniego błędu. */

    /**
     * Domyślny konstruktor.
     * @param htmlReport Raport sformatowany w HTML.
     */
    public Reports(HtmlReport htmlReport) {
        this.htmlReport = htmlReport;
    }

    /**
     * Getter.
     * @return Zwraca opis ostatniego błędu.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Metoda zapisująca raport do pliku .pdf.
     * @param filepath Nazwa i ścieżka do tworzonego pliku .pdf.
     * @return Zwraca true jeżeli operacja zakończyła się sukcesem.
     */
    public boolean saveReportToFile(String filepath) {

        filepath = filepath.replaceAll("\\.+.*$", "");
        filepath += ".pdf";

        String html = htmlReport.getHtmlReport();

        if(html.isEmpty()) {
            lastError = "Raport nie został wygenerowany.";
            return false;
        }

        try {
            ConverterProperties converterProperties = new ConverterProperties();
            FontProvider fontProvider = new DefaultFontProvider(false, false, false);
            fontProvider.addDirectory(FONT_DIR_PATH);

            converterProperties.setFontProvider(fontProvider);

            List<IElement> elements = HtmlConverter.convertToElements(html, converterProperties);
            PdfDocument pdf = new PdfDocument(new PdfWriter(filepath));

            pdf.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdf);

            for (IElement element : elements) {
                document.add((IBlockElement)element);
            }
            document.close();
            return true;
        }
        catch (IOException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }
}
