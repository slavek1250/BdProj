package com.bdproj;

public class SkiLiftAdmin {
    private SystemUser systemUser;

    // TODO: Dodawanie nowego wyciagu. #KLAUDIA#
    // TODO: Pobieranie istniejacych wyciagow bedacych pod kierownikiem systemUser.getId(). #KLAUDIA#
    // TODO: Modyfikacja wyciagow bedacych pod jurysdykcja bierzacego kierownika.
    // TODO: Usuwanie wyciagow (ustawienie flagi).

    public SkiLiftAdmin(SystemUser user) {
        systemUser = user;
    }
}
