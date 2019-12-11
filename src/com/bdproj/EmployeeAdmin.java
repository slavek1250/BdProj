package com.bdproj;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class EmployeeAdmin {
    private SystemUser systemUser;
    private String name;
    private String surname;
    private String login;

    // TODO: Dodawanie nowego pracownika. !!DONE!!
    // TODO: Modyfikacja obecnych pracownikow. !!DONE!!
    // TODO: Usuwanie pracownikow (ustawianie flagi). !!DONE!!
    // TODO: Pobieranie listy przy starcie id imie nazwisko podleglych pod systemUser.getId(). !!DONE!!

    public EmployeeAdmin(SystemUser user) {
        systemUser = user;

    }
    public String getName(){ return name;}
    public String getSurname(){return surname;}
    public String getLogin(){return login;}
    public void setName(String name){this.name=name;}
    public void setSurname(String surname){this.surname=surname;}
    public void setLogin(String login){this.login=login;}


    public void addNewUser(String name, String surname,String username,String password){
        PreparedStatement ps;
        String query="INSERT INTO pracownicy (nazwisko,imie,login,haslo,kierownik_id) VALUES (?,?,?,MD5(?),?)";
        int id=systemUser.getId();
        if(MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setString(1, surname);
                ps.setString(2, name);
                ps.setString(3, username);
                ps.setString(4, password);
                ps.setInt(5, id);
                int rs = ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkSameLogin(String login){
        boolean ans=false;
        PreparedStatement ps;
        ResultSet rs;
        String query="SELECT login FROM pracownicy WHERE login=?";
        if(MySQLConnection.prepareConnection()) {
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

public ArrayList<String> getEmployees(){

    PreparedStatement ps;
    ResultSet rs;
    String query="SELECT CONCAT(p.imie,' ', p.nazwisko,'    ',p.login) AS 'pracownik' FROM pracownicy p JOIN kierownik k ON p.kierownik_id=k.id WHERE kierownik_id=? AND p.zwolniony=0";
    ArrayList<String> emp =new ArrayList<String>();
    if(MySQLConnection.prepareConnection()) {
        try {
            ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1, systemUser.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                int i = 1;
                emp.add(rs.getString(i));
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    return emp;
}
    public void splitSelected (String user){
        String [] splitUser=user.split("\\s+");
        String name = splitUser[0];
        String surname = splitUser[1];
        String login = splitUser[2];
        setName(name);
        setSurname(surname);
        setLogin(login);
    }


    public void saveModChanges (String name,String surname){

        String givenLogin=getLogin();
        PreparedStatement ps;
        String query="UPDATE pracownicy SET imie=?, nazwisko=?, login=? WHERE login=?";
        if(MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setString(1, name);
                ps.setString(2, surname);
                ps.setString(3, login);
                ps.setString(4, givenLogin);
                int rs = ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteEmployee(){
        PreparedStatement ps;
        ResultSet rs;
        String query ="SELECT zwolniony FROM pracownicy WHERE login=?";
        if(MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setString(1, login);
                rs = ps.executeQuery();
                if (rs.first()) {
                    int zab = rs.getInt("zwolniony");
                    if (zab == 1) {
                        JOptionPane.showMessageDialog(null, "Ten pracownik jest już zwolniony!");
                    } else {
                        int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zwolnić pracownika?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.YES_OPTION) {
                            String query1 = "UPDATE pracownicy SET zwolniony=1 WHERE login=?";
                            PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1);
                            ps1.setString(1, login);
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
}
