package com.bdproj;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Tickets {
    private SystemUser systemUser;


    // TODO: Generowanie nowego biletu, generacja id.
    // TODO: Pobieranie bierzacego cennika.
    // TODO: Doladowywanie biletu.
    // TODO: Blokowanie biletow (ustawienie flagi).

    // Pobieranie najnowszego cennika z bazy:
    // select pc.id as 'poz_cennik_id', sc.nazwa, pc.cena from poz_cennik pc join slownik_cennik sc on pc.slownik_cennik_id = sc.id where pc.cennik_id = (select max(c.id) from cennik c);
   public String ticketNoIncrement () {
       PreparedStatement ps;
       ResultSet rs;
       String out="0";
       String query = "SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = \"slavek_bd2\" AND TABLE_NAME = \"karnet\"";
       try {
           ps = MySQLConnection.getConnection().prepareStatement(query);
           rs = ps.executeQuery();

           if (rs.first()) {
               out=(rs.getString(1));
           }
          else{
              out="NIE MA ŻADNEGO BILETU";
           }

       } catch (
               SQLException e1) {
           e1.printStackTrace();
       }
       return out;
   }


    public Tickets(SystemUser user) {
        systemUser = user;
    }
}
