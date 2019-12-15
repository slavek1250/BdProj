package com.bdproj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemUser {

    // TODO: Sprawdzanie czy pracownik nie zostal zawolniony #Karol# !!DONE!!

    public enum UserType {
        UNREGISTERED,
        EMPLOYEE,
        SUPERVISOR
    }

    private String lastError;
    private String name, surname;
    private String newName, newSurname;
    private int id = -1;
    private UserType userType = UserType.UNREGISTERED;

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getId() {
        return id;
    }

    public String getLastError() {
        return lastError;
    }

    public UserType getUserType() {
        return userType;
    }

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

    public void logOut() {
        id = -1;
        name = "";
        surname = "";
        userType = UserType.UNREGISTERED;
    }

    public void updateName(String name) {
        newName = name;
    }

    public void updateSurname(String surname) {
        newSurname = surname;
    }

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

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }
        if(userType != UserType.SUPERVISOR) {
            lastError = "Nie jesteś kierownikem!";
            return false;
        }

        try {
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
