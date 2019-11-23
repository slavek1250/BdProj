package com.bdproj;

import java.sql.DriverManager;
import java.sql.Connection;
public class MySQLConnection {

    public static Connection getConnection(){

        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("mn16.webd.pl:3306", "slavek_bd2_admin", "bd2@2019");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return con;
    }

}

