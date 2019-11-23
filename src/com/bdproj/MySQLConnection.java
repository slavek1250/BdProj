package com.bdproj;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnection {

    public static Connection getConnection() throws NamingException, SQLException {

        Context context = new InitialContext();
        //DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc/myDB");


        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://mn16.webd.pl:3306", "slavek_bd2_user", "bd2@2020");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println(con.isClosed());

        return con;
    }

}

