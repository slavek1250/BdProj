package com.bdproj;

/**
 * Klasa reprezentująca pracownika.
 */
public class Employee {
    protected SystemUser systemUser;    /**< Obecnie zalogowany użytkownik systemu. */
    protected Tickets tickets;          /**< Obiekt odpowiedzialny za obsługę biletów. */

    /**
     * Domyślny konstruktor.
     * @param user Obiekt obecnie zalogowanego użytkownika.
     */
    public Employee(SystemUser user) {
        systemUser = user;
        tickets = new Tickets(user);
    }
}
