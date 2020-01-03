package com.bdproj;
import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Klasa odpowiedzialna za dodawanie, usuwanie i modyfikacje pracowników.
 */

public class EmployeeAdmin {
    private SystemUser systemUser; /**< Obiekt obecnie zalogowanego użytkownika systemu. */
    /**
     * Metoda zapisująca dane obecnie zalogowanego kierownika.
     * @param user Obiekt zawierający dane obecnie zalogowanego użytkownika.
     */
    public EmployeeAdmin(SystemUser user) {systemUser = user; }

    /**
     * Metoda służąca do dodawania nowego pracownika.
     * @param name Imie nowego pracownika.
     * @param surname Nazwisko nowego pracownika.
     * @param username Login nowego pracownika.
     * @param password Hasło nowego pracownika.
     */
    public void addNewUser(String name, String surname, String username, String password) {
        PreparedStatement ps;
        String query = "INSERT INTO pracownicy (nazwisko,imie,login,haslo,kierownik_id) VALUES (?,?,?,MD5(?),?)";
        int id = systemUser.getId();
        if (MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setString(1, surname);
                ps.setString(2, name);
                ps.setString(3, username);
                ps.setString(4, password);
                ps.setInt(5, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda sprawdzająca czy istnieje w bazie dokładnie taki sam login.
     * @param login Login którego poprawność ma zostać sprawdzona.
     * @return Zwraca true, jeżeli w bazie istnieje dokładnie taki sam login.
     */
    public boolean checkSameLogin(String login) {
        boolean ans = false;
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT login FROM pracownicy WHERE id=?";
        if (MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setString(1, login);
                rs = ps.executeQuery();
                if (rs.first()) {
                    ans = true;
                } else {
                    ans = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ans;
    }

    /**
     * Metoda sprawdzająca zgodność haseł.
     * @param id Id kierownika, dla którego ma zostać sprawdzone hasło.
     * @param password Hasło kierownika.
     * @return Zwraca true, jeżeli hasła są takie same.
     */
    public boolean checkSamePassword(int id, String password) {
        boolean ans = false;
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT * FROM kierownik WHERE id=? AND haslo=MD5(?)";
        if (MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setInt(1, id);
                ps.setString(2, String.valueOf(password));
                rs = ps.executeQuery();
                if (rs.first()) {
                    ans = true;
                } else {
                    ans = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ans;
    }

    /**
     * Metoda zapisująca modyfikacje danych pracownika.
     * @param id Id pracownika dla którego zmieniane są dane.
     * @param name Nowe imie pracownika.
     * @param surname Nowe nazwisko pracownika.
     */
    public void saveModChanges(int id, String name, String surname) {
        int givenLogin = id;
        PreparedStatement ps;
        String query = "UPDATE pracownicy SET imie=?, nazwisko=? WHERE id=?";
        if (MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setString(1, name);
                ps.setString(2, surname);
                ps.setInt(3, givenLogin);
                int rs = ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda usuwająca pracownika.
     * @param id Id pracownika który ma zostać usunięty.
     */
    public void deleteEmployee(int id) {
        PreparedStatement ps;
        ResultSet rs;
        String query = "SELECT zwolniony FROM pracownicy WHERE id=?";
        if (MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setInt(1, id);
                rs = ps.executeQuery();
                if (rs.first()) {
                    int zab = rs.getInt("zwolniony");
                    if (zab == 1) {
                        JOptionPane.showMessageDialog(null, "Ten pracownik jest już zwolniony!");
                    } else {
                        int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zwolnić pracownika?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.YES_OPTION) {
                            String query1 = "UPDATE pracownicy SET zwolniony=1 WHERE id=?";
                            PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1);
                            ps1.setInt(1, id);
                            int rs1 = ps1.executeUpdate();

                        } else { return;}
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Metoda odpowiedzialna za awansowanie pracownika na kierownika. Przepisywane są dane do tabeli z kierownikami i status pracownika jest ustawiany na zwolniony.
     * @param id Id pracownika który ma zostać awansowany.
     */
    public void promoteToSupervisor(int id) {
        String query1 = "INSERT INTO kierownik (nazwisko,imie,login,haslo) SELECT nazwisko,imie,login,haslo FROM pracownicy WHERE id=?";
        if (MySQLConnection.prepareConnection()) {
            try {
                PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1);
                ps1.setInt(1, id);
                ps1.executeUpdate();
                String query = "UPDATE pracownicy SET zwolniony=1 WHERE id=?";
                PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda służąca do podległego pracownika do innego kierownika.
     * @param empId Id pracownika który ma zostać przeniesiony.
     * @param supId Id kierownika do którego ma zostać przeniesiony pracownik.
     */
    public void changeEmployeeSupervisor(int empId, int supId) {
        String query = "UPDATE pracownicy SET kierownik_id=? WHERE id=?";
        if (MySQLConnection.prepareConnection()) {
            try {
                PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setInt(1, supId);
                ps.setInt(2, empId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda do zmiany hasła pracownika.
     * @param id Id pracownika dla którego hasło ma zostać zmienione.
     * @param pass Nowo wygenerowane hasło pracownika.
     */
    public void changeEmployeePass(int id,String pass) {

        String query = "UPDATE pracownicy SET haslo=MD5(?) WHERE id=?";
        if (MySQLConnection.prepareConnection()) {
            try {
                PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setString(1,pass);
                ps.setInt(2, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


