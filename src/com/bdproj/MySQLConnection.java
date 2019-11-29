package com.bdproj;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnection {

    private static String databaseType = "jdbc:mysql";
    private static String serverAddress = "mn16.webd.pl";
    private static int serverPort = 3306;
    private static String database = "slavek_bd2";
    private static String databaseUser = "slavek_bd2_user";
    private static String databaseUserPass = "bd2@2020";
    private static Connection connection = null;
    private static String lastError;

    public static boolean prepareConnection() {
        try {
            if (connection == null || connection.isClosed()) {
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
}