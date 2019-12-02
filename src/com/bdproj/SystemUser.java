package com.bdproj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemUser {

    // TODO: Sprawdzanie czy pracownik nie zostal zawolniony

    public enum UserType {
        UNREGISTERED,
        EMPLOYEE,
        SUPERVISOR
    }

    private String lastError;
    private String name, surname;
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

    public UserType getUserType() {
        return userType;
    }

    public UserType logIn(String login, String password) {

        PreparedStatement ps;
        ResultSet rs;
        String query =  "SELECT 'K' as WHO, id, imie, nazwisko FROM kierownik WHERE login LIKE ? AND haslo LIKE MD5(?)\n" +
                        "union all\n" +
                        "SELECT 'P' as WHO, id, imie, nazwisko FROM pracownicy WHERE login LIKE ? AND haslo LIKE MD5(?)";

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
}
