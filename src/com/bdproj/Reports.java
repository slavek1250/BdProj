package com.bdproj;

import java.io.File;
import java.util.Date;

public class Reports {

    SystemUser systemUser;

    // TODO: Generowanie raportow uzyc poszczegolnych wyciagow od do. Ile zarobil, ile razy uzyto, srednie dzienne, tygodniowe, itd. #Dominik#
    // TODO: Generowanie reportow uzyc poszczegolnych biletow, ile km przejechane (przewyzszenie) ile wydano na pkt, srednie, itd. #Dominik#

    private String lastError;

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

        return true;
    }

    public boolean saveReportToFile(String filepath) {

        filepath = filepath.replaceAll("\\.+.*$", "");
        filepath += ".pdf";

        System.out.println(filepath);

        return false;
    }
}
