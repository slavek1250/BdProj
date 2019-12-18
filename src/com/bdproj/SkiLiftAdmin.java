package com.bdproj;
import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
public class SkiLiftAdmin {
    private SystemUser systemUser;

    // TODO: Dodawanie nowego wyciagu. #KLAUDIA# !!DONE!!
    // TODO: Pobieranie istniejacych wyciagow bedacych pod kierownikiem systemUser.getId(). #KLAUDIA# !!DONE!!
    // TODO: Modyfikacja wyciagow bedacych pod jurysdykcja bierzacego kierownika. #SZYMON#
    // TODO: Usuwanie wyciagow (ustawienie flagi). #SZYMON#

    public SkiLiftAdmin(SystemUser user) { systemUser = user; }

    public void addNewLift(String name, String height, String pointsCost, boolean state, int idSup){
        PreparedStatement ps1;
        PreparedStatement ps2;
        PreparedStatement ps3;
        ResultSet rs;
        int idLift = 0;
        String sql_1 = "INSERT INTO wyciag (nazwa, wysokosc) VALUES(?,?)";
        String sql_2 = "INSERT INTO wyciag_dane (od, koszt_pkt, stan, wyciag_id, kierownik_id) VALUES(now(),?,?,?,?)";
        String sql_3 = "SELECT id FROM wyciag WHERE nazwa=? AND wysokosc =?";
        if(MySQLConnection.prepareConnection()) {
            try {
                ps1 = MySQLConnection.getConnection().prepareStatement(sql_1);
                ps1.setString(1, name);
                ps1.setString(2, height);
                ps1.executeUpdate();
                ps3 = MySQLConnection.getConnection().prepareStatement(sql_3);
                ps3.setString(1,name);
                ps3.setString(2,height);
                rs = ps3.executeQuery();
                if(rs.first()){
                    idLift = rs.getInt("id");
                }
                ps2 = MySQLConnection.getConnection().prepareStatement(sql_2);
                ps2.setString(1, pointsCost);
                ps2.setBoolean(2, state);
                ps2.setInt(3, idLift);
                ps2.setInt(4,idSup);
                ps2.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}

