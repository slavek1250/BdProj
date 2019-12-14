package com.bdproj;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;

import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;


public class Reports {

    HtmlReport htmlReport;

    // TODO: Generowanie raportow uzyc poszczegolnych wyciagow od do. Ile zarobil, ile razy uzyto, srednie dzienne, tygodniowe, itd. #Dominik#
    // TODO: Generowanie reportow uzyc poszczegolnych biletow, ile km przejechane (przewyzszenie) ile wydano na pkt, srednie, itd. #Dominik#

    private String lastError;

    public Reports(HtmlReport htmlReport) {
        this.htmlReport = htmlReport;
    }

    public String getLastError() {
        return lastError;
    }
/*
    public boolean generateSkiLiftReport(Integer id, Date since, Date to) {

        return true;
    }

    public boolean generateTicketUseReport(Integer ticketId) {

        try {
            html = Files.readString(Paths.get("E:\\studia_lab\\sem_V_lab\\BDProj\\JavaProgram\\src\\com\\bdproj\\test.html"));
        }
        catch (IOException ex) {
            lastError = ex.getMessage();
            return false;
        }
        return true;
    }
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
            //FontProgram fontProgram = FontProgramFactory.createFont("AbhayaLibre-Regular.ttf");
            //fontProvider.addFont(fontProgram);
            fontProvider.addDirectory("reports/font");

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
