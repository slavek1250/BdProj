package com.bdproj;

public class SkiLiftAdmin {
    private SystemUser systemUser;

    // TODO: Dodawanie nowego wyciagu. #KLAUDIA#
    // TODO: Pobieranie istniejacych wyciagow bedacych pod kierownikiem systemUser.getId(). #KLAUDIA#
    // TODO: Modyfikacja wyciagow bedacych pod jurysdykcja bierzacego kierownika. #SZYMON#
    // TODO: Usuwanie wyciagow (ustawienie flagi). #SZYMON#

    public SkiLiftAdmin(SystemUser user) {
        systemUser = user;
    }
}
