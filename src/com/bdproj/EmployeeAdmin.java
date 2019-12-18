package com.bdproj;
import javafx.util.Pair;
import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class EmployeeAdmin {
    private SystemUser systemUser;


    // TODO: Dodawanie nowego pracownika.#Karol# !!DONE!!
    // TODO: Modyfikacja obecnych pracownikow.#Karol# !!DONE!!
    // TODO: Usuwanie pracownikow (ustawianie flagi).#Karol# !!DONE!!
    // TODO: Pobieranie listy przy starcie id imie nazwisko podleglych pod systemUser.getId().#Karol# !!DONE!!
    // TODO: Awansowanie pracownika na kierownika. #Karol# !!DONE!!
    // TODO: Przypisanie danego pracownika do innego kierownika. #Karol# !!DONE!!
    // TODO: Zmiana hasła dla pracownika. #Karol# !!DONE!!

    public EmployeeAdmin(SystemUser user) {
        systemUser = user;

    }

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

    public boolean checkSameLPassword(int id, String password) {
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

                        } else {
                            return;
                        }
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

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


