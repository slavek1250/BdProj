package com.bdproj;

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


public class Reports {

    HtmlReport htmlReport;

    // TODO: Generowanie raportow uzyc poszczegolnych wyciagow od do. Ile zarobil, ile razy uzyto, srednie dzienne, tygodniowe, itd. #Dominik# !!DONE!!
    // TODO: Generowanie reportow uzyc poszczegolnych biletow, ile km przejechane (przewyzszenie) ile wydano na pkt, srednie, itd. #Dominik# !!DONE!!

    private String lastError;

    public Reports(HtmlReport htmlReport) {
        this.htmlReport = htmlReport;
    }

    public String getLastError() {
        return lastError;
    }

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
