package com.bdproj;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MySQLConnection {

    private static String databaseType = "jdbc:mysql";
    private static String serverAddress = "mn16.webd.pl"; // localhost / mn16.webd.pl
    private static int serverPort = 3306;
    private static String database = "slavek_bd2";
    private static String databaseUser = "slavek_bd2_user";
    private static String databaseUserPass = "bd2@2020";
    private static Connection connection = null;
    private static String lastError;

    public static boolean prepareConnection() {
        try {
            if (connection == null || !connection.isValid(2)) {
                connection = DriverManager.getConnection(
                        (databaseType + "://" + serverAddress + ":" + serverPort + "/" + database),
                        databaseUser,
                        databaseUserPass
                );
            }
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static String getLastError() {
        return lastError;
    }

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