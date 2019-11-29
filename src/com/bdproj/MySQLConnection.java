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

    public static Connection getConnection() throws SQLException {

        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            con = DriverManager.getConnection(
                    (databaseType + "://" + serverAddress + ":" + serverPort + "/" + database),
                    databaseUser,
                    databaseUserPass
            );
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return con;
    }

}

