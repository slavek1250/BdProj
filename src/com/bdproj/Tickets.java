package com.bdproj;

public class Tickets {
    private SystemUser systemUser;

    // TODO: Generowanie nowego biletu, generacja id.
    // TODO: Pobieranie bierzacego cennika.
    // TODO: Doladowywanie biletu.
    // TODO: Blokowanie biletow (ustawienie flagi).

    // Pobieranie najnowszego cennika z bazy:
    // select pc.id as 'poz_cennik_id', sc.nazwa, pc.cena from poz_cennik pc join slownik_cennik sc on pc.slownik_cennik_id = sc.id where pc.cennik_id = (select max(c.id) from cennik c);

    
    public Tickets(SystemUser user) {
        systemUser = user;
    }
}
