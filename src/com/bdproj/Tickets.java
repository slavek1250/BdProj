package com.bdproj;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Tickets {
    private SystemUser systemUser;


    // TODO: Generowanie nowego biletu, generacja id. #KLAUDIA#
    // TODO: Pobieranie bierzacego cennika. #KLAUDIA#
    // TODO: Doladowywanie biletu. #KLAUDIA#
    // TODO: Blokowanie biletow (ustawienie flagi).#Karol# !!DONE!!

    // Pobieranie najnowszego cennika z bazy:
    // select pc.id as 'poz_cennik_id', sc.nazwa, pc.cena from poz_cennik pc join slownik_cennik sc on pc.slownik_cennik_id = sc.id where pc.cennik_id = (select max(c.id) from cennik c);
    public String ticketNoIncrement () {
        PreparedStatement ps;
        ResultSet rs;
        String out="0";
        String query = "SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = \"slavek_bd2\" AND TABLE_NAME = \"karnet\"";
        if(MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                rs = ps.executeQuery();

                if (rs.first()) {
                    out = (rs.getString(1));
                } else {
                    out = "W bazie nie ma żadnych biletów";
                }

            } catch (
                    SQLException e1) {
                e1.printStackTrace();
            }
        }
        return out;
    }
public void blockTicket (String ticketnumber){
    PreparedStatement ps;
    ResultSet rs;
    String query ="SELECT zablokowany FROM karnet WHERE id=?";
    if(MySQLConnection.prepareConnection()) {
        try {
            ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setString(1, ticketnumber);
            rs = ps.executeQuery();
            if (rs.first()) {
                int zab = rs.getInt("zablokowany");
                if (zab == 1) {
                    JOptionPane.showMessageDialog(null, "Ten bilet jest już zablokowany");
                } else {
                    String query1 = "UPDATE karnet SET zablokowany=1 WHERE id=?";
                    PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1);
                    ps1.setString(1, ticketnumber);
                    int rs1 = ps1.executeUpdate();
                    JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zablokować bilet");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Nie ma takiego biletu");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

    public Tickets(SystemUser user) {
        systemUser = user;
    }
}