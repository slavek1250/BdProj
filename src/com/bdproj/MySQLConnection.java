package com.bdproj;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Klasa odpowiedzialna za połączenie z bazą danych.
 */
public class MySQLConnection extends MySQLConnParams {

    private static String databaseType = "jdbc:mysql";  /**< Typ bazy danych. */
    private static Connection connection = null;        /**< Obiekt połączenia z bazą danych. */
    private static String lastError;                    /**< Opis ostatniego błędu. */

    /**
     * Metoda odpowiedzialna za zestawnienie połączenia z bazą danych.
     * @return Zwraca true jeżeli ustanowiono połączenie.
     */
    public static boolean prepareConnection() {
        try {
            if (connection == null || !connection.isValid(2)) {
                connection = DriverManager.getConnection(
                        (databaseType + "://" + getServerAddress() + ":" + getServerPort() + "/" + getDatabase() +"?useUnicode=true&characterEncoding=UTF-8"),
                        getDatabaseUser(),
                        getDatabaseUserPass()
                );
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
     * @return Zwraca obiekt połącznenia z bazą danych.
     * @see prepareConnection()
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Getter.
     * @return Opis ostatniego błędu.
     */
    public static String getLastError() {
        return lastError;
    }

    /**
     * Getter.
     * @return Bieżący czas serwera bazy danych.
     * @throws SQLException
     */
    public static Date getServerTimestamp() throws SQLException {
        String sqlDateFormat = "%Y-%m-%d %H:%i:%s.%f";
        String javaDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";

        String query = "select DATE_FORMAT(now(), ?);";
        PreparedStatement ps = getConnection().prepareStatement(query);
        ps.setString(1, sqlDateFormat);
        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            try {
                return (new SimpleDateFormat(javaDateFormat)).parse(rs.getString(1));
            }
            catch (ParseException ex) {
                throw  new SQLException(ex.getMessage());
            }
        }
        throw new SQLException("Could not get server time.");
    }
}