package com.bdproj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Klasa reprezentująca kierownika.
 */
public class Supervisor {
    protected SystemUser systemUser;        /**< Obiekt obecenie zalogowanego użytkownika. */
    protected PriceList priceList;          /**< Obiekt cennika. */
    protected EmployeeAdmin employeeAdmin;  /**< Obiekt administratora pracowników. */
    protected SkiLiftAdmin skiLiftAdmin;    /**< Obiekt administratora wyciągów. */

    private String lastError;               /**< Opis ostatniego błędu. */

    /**
     * Dane o kierownikach przechowywane lokalnie.
     */
    protected enum SupervisorsListEnum {
        ID,     /**< Numer id kierownika. */
        NAME,   /**< Imie kierownika. */
        SURNAME /**< Nazwisko kierownika. */
    };
    /**
     * Lista wszystkich niezwolnionych kierowników.
     */
    protected ArrayList<EnumMap<SupervisorsListEnum, String>> supervisorsList = null;

    /**
     * Dane o pracownikach przechowywane lokalnie.
     */
    protected enum EmployeeListEnum {
        ID,     /**< Numer id pracownika. */
        NAME,   /**< Imie pracownika. */
        SURNAME /**< Nazwisko pracownika. */
    };
    /**
     * Lista niezwolnionych pracowników podległych obecnie zalogowanemu kierownikowi.
     */
    protected ArrayList<EnumMap<EmployeeListEnum, String>> employeeList = null;

    /**
     * Domyślny konstruktor.
     * @param user Obiekt obecnie zalogowanego użytkownika systemu.
     */
    public Supervisor(SystemUser user) {
        systemUser = user;
        priceList = new PriceList(user);
        employeeAdmin = new EmployeeAdmin(user);
        skiLiftAdmin = new SkiLiftAdmin(user);
    }

    /**
     * Getter.
     * @param id Numer id kieronika.
     * @return Imie kierownika, jeżeli brak kierownika o podanym numerze id zwraca "".
     * @see fetchSupervisors()
     */
    protected String getSupervisorName(Integer id) {
        EnumMap<SupervisorsListEnum, String> supervisor = supervisorsList.stream()
                .filter(sv -> id.toString().equals(sv.get(SupervisorsListEnum.ID)))
                .findAny()
                .orElse(null);
        return supervisor == null ? "" : supervisor.get(SupervisorsListEnum.NAME);
    }

    /**
     * Getter.
     * @param id Numer id kieronika.
     * @return Nazwisko kierownika, jeżeli brak kierownika o podanym numerze id zwraca "".
     * @see fetchSupervisors()
     */
    protected String getSupervisorSurname(Integer id) {
        EnumMap<SupervisorsListEnum, String> supervisor = supervisorsList.stream()
                .filter(sv -> id.toString().equals(sv.get(SupervisorsListEnum.ID)))
                .findAny()
                .orElse(null);
        return supervisor == null ? "" : supervisor.get(SupervisorsListEnum.SURNAME);
    }

    /**
     * Getter.
     * @return Zwraca opis ostatniego błędu.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Metoda odpowiedzialna za pobranie listy wszystkich niezwolnionych kierowników z bazy danych.
     * @return Zwraca true jeżeli operacja zakończyła się sukcesem.
     */
    protected boolean fetchSupervisors() {
        String query = "select id, nazwisko, imie from kierownik where zwolniony = 0 AND id!=?;";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1,systemUser.getId());
            ResultSet rs = ps.executeQuery();

            supervisorsList = new ArrayList<>();

            while (rs.next()) {
                EnumMap<SupervisorsListEnum, String> tmp = new EnumMap<>(SupervisorsListEnum.class);
                tmp.put(SupervisorsListEnum.ID, rs.getString("id"));
                tmp.put(SupervisorsListEnum.NAME, rs.getString("imie"));
                tmp.put(SupervisorsListEnum.SURNAME, rs.getString("nazwisko"));
                supervisorsList.add(tmp);
            }
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    /**
     * Getter.
     * @param id Numer id pracownika.
     * @return Imie kierownika, jeżeli brak kierownika o podanym numerze id zwraca "".
     * @see fetchEmployees()
     */
    protected String getEmployeeName(Integer id) {
        return employeeList
                .stream()
                .filter(sv -> id.equals(Integer.parseInt(sv.get(EmployeeListEnum.ID))))
                .findAny()
                .map(sv -> sv.get(EmployeeListEnum.NAME))
                .orElse("");
    }

    /**
     * Getter.
     * @param id Numer id pracownika.
     * @return Nazwisko kierownika, jeżeli brak kierownika o podanym numerze id zwraca "".
     * @see fetchEmployees()
     */
    protected String getEmployeeSurname(Integer id) {
        return employeeList
                .stream()
                .filter(sv -> id.equals(Integer.parseInt(sv.get(EmployeeListEnum.ID))))
                .findAny()
                .map(sv -> sv.get(EmployeeListEnum.SURNAME))
                .orElse("");
    }

    /**
     * Metoda odpowiedzialna za pobranie listy niezwolnionych pracowników podległych obecnie zalogowanemu kierownikowi.
     * @return Zwraca true jeżeli operacja zakończyła się sukcesem.
     */
    public boolean fetchEmployees(){

        String query="select id, nazwisko, imie from pracownicy where zwolniony = 0 and kierownik_id=? ";
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        if(MySQLConnection.prepareConnection()) {
            try {
               PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setInt(1,systemUser.getId());
               ResultSet rs = ps.executeQuery();
                employeeList =new ArrayList<>();
                while (rs.next()) {
                    EnumMap<EmployeeListEnum, String> tmp = new EnumMap<>(EmployeeListEnum.class);
                    tmp.put(EmployeeListEnum.ID, rs.getString("id"));
                    tmp.put(EmployeeListEnum.NAME, rs.getString("imie"));
                    tmp.put(EmployeeListEnum.SURNAME, rs.getString("nazwisko"));
                    employeeList.add(tmp);
                }
                return true;
            } catch (SQLException e) {
                lastError=e.getMessage();
            }
        }
        return false;
    }
}
