package com.bdproj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Klasa użytkownika systemu.
 */
public class SystemUser {

    /**
     * Typ użytkownika systemu.
     */
    public enum UserType {
        UNREGISTERED,   /**< Uzytkownik niezarejestrowany. */
        EMPLOYEE,       /**< Zwykły pracownik. */
        SUPERVISOR      /**< Kierownik. */
    }

    private String lastError;                           /**< Opis ostatniego błędu. */
    private String name, surname;                       /**< Imię i nazwisko użytkownika systemu. */
    private String newName, newSurname;                 /**< Zmodyfikowane imię i nazwisko. */
    private int id = -1;                                /**< Numer id użytkownika systemu. */
    private UserType userType = UserType.UNREGISTERED;  /**< Typ użytkownika systemu. */

    /**
     * Getter.
     * @return Zwraca imię użytkownika systemu.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter.
     * @return Zwraca nazwisko użytkownika systemu.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Getter.
     * @return Zwraca numer id użytkownika systemu.
     */
    public int getId() {
        return id;
    }

    /**
     * Getter.
     * @return Zwraca opis ostatniego błędu.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Getter.
     * @return Zwraca typ użytkownika systemu.
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Metoda logowania.
     *  - Sprawdza czy użytkownik o podanym loginie i haśle istnieje w systemie i jest niezwolniony.
     *  - Jeżeli istnieje użytkownik w bazie to ustala czy jest on kierownikiem czy pracownikiem.
     * @param login Login użytkownika.
     * @param password Hasło użytkownika.
     * @return Typ użytkownika systemu.
     * @see logOut()
     */
    public UserType logIn(String login, String password) {

        PreparedStatement ps;
        ResultSet rs;
        String query =  "SELECT 'K' as WHO, id, imie, nazwisko FROM kierownik WHERE login LIKE ? AND haslo LIKE MD5(?) AND zwolniony = 0\n" +
                        "union all\n" +
                        "SELECT 'P' as WHO, id, imie, nazwisko FROM pracownicy WHERE login LIKE ? AND haslo LIKE MD5(?) AND zwolniony = 0";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return userType;
        }

        try {
            ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setString(1, login);
            ps.setString(2, password);
            ps.setString(3, login);
            ps.setString(4, password);
            rs = ps.executeQuery();

            if(rs.first()) {

                String who = rs.getString("WHO");

                if (who.equals("K")) {
                    userType = UserType.SUPERVISOR;
                } else if (who.equals("P")) {
                    userType = UserType.EMPLOYEE;
                }
                else {
                    return userType;
                }

                id = rs.getInt("id");
                name = rs.getString("imie");
                surname = rs.getString("nazwisko");
            }

        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return userType;
    }

    /**
     * Metoda wylogowywująca użytkownika.
     * @see logIn()
     */
    public void logOut() {
        id = -1;
        name = "";
        surname = "";
        userType = UserType.UNREGISTERED;
    }

    /**
     * Metoda aktualizująca imię użytkownika.
     * @param name Nowe imie.
     * @see commitChanges()
     */
    public void updateName(String name) {
        newName = name;
    }

    /**
     * Metoda aktualizująca nazwisko użytkownika.
     * @param surname Nowe nazwisko.
     * @see commitChanges()
     */
    public void updateSurname(String surname) {
        newSurname = surname;
    }

    /**
     * Metoda zapisująca do bazy danych zmienione dane użytkownika.
     * Prawo do aktualizacji sowich danych posiadają użytkownicy należący do grupy kierowników.
     * @return Zwraca true jeżeli operacja zakończyła się sukcesem.
     */
    public boolean commitChanges() {

        if(newName.isEmpty() && newSurname.isEmpty()) {
            lastError = "Dane nie zastały zmienione.";
            return false;
        }
        if(userType != UserType.SUPERVISOR) {
            lastError = "Nie jesteś kierownikem!";
            return false;
        }
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        String query = "UPDATE kierownik SET imie='" + (newName.isEmpty() ? name : newName) + "', nazwisko='" + (newSurname.isEmpty() ? surname : newSurname) + "' WHERE id=?";

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1, id);

            ps.execute();

            name = (newName.isEmpty() ? name : newName);
            surname = (newSurname.isEmpty() ? surname : newSurname);
            newName = null;
            newSurname = null;
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    /**
     * Metoda odpowiedzialna za ustawinie flagi w bazie że kierownik się zwolnił z pracy.
     * Prawo do wykonania tej metody posiadają użytkownicy należący do grupy kierowników.
     * Przed ustawnienim flagi sprawdzane jest czy:
     *  - istenieją w systemie inni niezwolnieni kierownicy.
     *  - kierownik ma pod sobą jakiegokolwiek pracownika, jeżeli tak anuluje operację.
     *  - kierownik jest jedynym zarzadcą jakiegokolwiek wyciągu, jeżeli tak anuluje operację.
     * @return Zwraca true jeżeli wykonywanie operacji zakończyło się sukecesem.
     */
    public boolean quitJob() {

        String query1 = "select z1.wyciag_id, w.nazwa\n" +
                        "from zarzadcy z1 join wyciag w on z1.wyciag_id = w.id\n" +
                        "where z1.wyciag_id = any \n" +
                        "( select distinct z2.wyciag_id from zarzadcy z2 where z2.kierownik_id = ? )\n" +
                        "and z1.od < now() and (z1.do is null or z1.do > now())\n" +
                        "group by z1.wyciag_id, w.nazwa having count(z1.id) = 1;";
        String query2 = "select concat(imie, ' ', nazwisko, ' - ', login) as 'pracownik' from pracownicy where zwolniony = 0 and kierownik_id = ?;";
        String query3 = "update kierownik set zwolniony = 1 where id = ?;";
        String query4 = "update zarzadcy set do=now() where kierownik_id = ?;";
        String query5 = "select count(k.id) as l_kier from kierownik k where k.zwolniony=0;";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }
        if(userType != UserType.SUPERVISOR) {
            lastError = "Nie jesteś kierownikem!";
            return false;
        }

        try {
            PreparedStatement ps5 = MySQLConnection.getConnection().prepareStatement(query5);
            ResultSet rs5 = ps5.executeQuery();
            if(rs5.next()) {
                int supervisorsCount = rs5.getInt("l_kier");
                if(supervisorsCount == 1) {
                    lastError = "Jesteś jednynym niezwolnionym kierownikiem.\nNie możesz zwolnić się z pracy.";
                    return false;
                }
            }

            PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1);
            ps1.setInt(1, id);
            ResultSet rs1 = ps1.executeQuery();

            String skiLiftsList = "";
            int i = 0;
            while (rs1.next()) {
                skiLiftsList += rs1.getString("nazwa") + ",\n";
                i++;
            }

            PreparedStatement ps2 = MySQLConnection.getConnection().prepareStatement(query2);
            ps2.setInt(1, id);
            ResultSet rs2 = ps2.executeQuery();

            String employeesList = "";
            int j = 0;
            while(rs2.next()) {
                employeesList += rs2.getString("pracownik") + ",\n";
                j++;
            }

            if(i > 0 || j > 0) {
                if(i > 0) {
                    skiLiftsList = skiLiftsList.replaceAll(",\n$", "");
                    skiLiftsList = "Jesteś jedynym nadzorcą " + (i == 1 ? "wyciągu:\n" : "wyciągów:\n") + skiLiftsList + ".\n\n";
                }
                if(j > 0) {
                    employeesList = employeesList.replaceAll(",\n$", "");
                    employeesList = "Jesteś kierownikiem " + (j == 1 ? "pracownika:\n" : "następujących pracowników:\n") + employeesList + ".\n\n";
                }

                lastError = skiLiftsList + employeesList + "Nie możesz się zwolnić.";
                return false;
            }

            PreparedStatement ps3 = MySQLConnection.getConnection().prepareStatement(query3);
            ps3.setInt(1, id);
            ps3.execute();

            PreparedStatement ps4 = MySQLConnection.getConnection().prepareStatement(query4);
            ps4.setInt(1, id);
            ps4.execute();

            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }

        return false;
    }
}
