package com.bdproj;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;

import java.util.Date;

public class Reports {

    SystemUser systemUser;

    // TODO: Generowanie raportow uzyc poszczegolnych wyciagow od do. Ile zarobil, ile razy uzyto, srednie dzienne, tygodniowe, itd. #Dominik#
    // TODO: Generowanie reportow uzyc poszczegolnych biletow, ile km przejechane (przewyzszenie) ile wydano na pkt, srednie, itd. #Dominik#

    private String lastError;

    private String html = null;

    public Reports(SystemUser user) {
        systemUser = user;
    }

    public String getLastError() {
        return lastError;
    }

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

    public boolean saveReportToFile(String filepath) {

        filepath = filepath.replaceAll("\\.+.*$", "");
        filepath += ".pdf";

        if(html.isEmpty()) {
            lastError = "Raport nie został wygenerowany.";
            return false;
        }

        try {

            List<IElement> elements =
                    HtmlConverter.convertToElements(html, null);
            PdfDocument pdf = new PdfDocument(new PdfWriter(filepath));
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
