package com.bdproj;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnection {

    public static Connection getConnection() throws SQLException {

        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://mn16.webd.pl:3306/slavek_bd2", "slavek_bd2_user", "bd2@2020");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return con;
    }

}

