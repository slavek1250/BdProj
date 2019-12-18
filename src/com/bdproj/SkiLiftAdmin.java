package com.bdproj;
import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;

public class SkiLiftAdmin {
    private SystemUser systemUser;
    public enum SkiLiftsListEnum { ID, NAME, POINTS, STATE };
    public ArrayList<EnumMap<SkiLiftsListEnum, String>> skiLiftsList = null;
    private String lastError;

    // TODO: Dodawanie nowego wyciagu. #KLAUDIA# !!DONE!!
    // TODO: Pobieranie istniejacych wyciagow bedacych pod kierownikiem systemUser.getId(). #KLAUDIA# !!DONE!!
    // TODO: Modyfikacja wyciagow bedacych pod jurysdykcja bierzacego kierownika. #SZYMON#
    // TODO: Usuwanie wyciagow (ustawienie flagi). #SZYMON#

    public SkiLiftAdmin(SystemUser user) { systemUser = user; }

    public boolean fetchSkiLifts() {

        String query = "select w.id, w.nazwa, wd.koszt_pkt, wd.stan\n" +
                "from wyciag w join wyciag_dane wd on w.id = wd.wyciag_id join zarzadcy z on w.id = z.wyciag_id\n" +
                "where wd.id = ( select wd2.id from wyciag_dane wd2 where wd2.wyciag_id = w.id order by wd2.od desc limit 1)\n" +
                "and z.do is null and wd.nieistniejacy = 0 and z.kierownik_id = ?";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1, systemUser.getId());
            ResultSet rs = ps.executeQuery();

            skiLiftsList = new ArrayList<>();

            while (rs.next()) {
                EnumMap<SkiLiftsListEnum, String> tmp = new EnumMap<>(SkiLiftsListEnum.class);
                tmp.put(SkiLiftsListEnum.ID, rs.getString("id"));
                tmp.put(SkiLiftsListEnum.NAME, rs.getString("nazwa"));
                tmp.put(SkiLiftsListEnum.POINTS,rs.getString("koszt_pkt"));
                tmp.put(SkiLiftsListEnum.STATE,rs.getString("stan"));
                skiLiftsList.add(tmp);
            }
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }
    public String getLastError() {
        return lastError;
    }

    protected Integer getSkiLiftId(String name) {
        EnumMap<SkiLiftsListEnum, String> skiLift = skiLiftsList.stream()
                .filter(lift -> name.equals(lift.get(SkiLiftsListEnum.NAME)))
                .findAny()
                .orElse(null);
        return skiLift == null ? -1 : Integer.parseInt(skiLift.get(SkiLiftsListEnum.ID));
    }

    protected String getSkiLiftName(Integer id) {
        EnumMap<SkiLiftsListEnum, String> skiLift = skiLiftsList.stream()
                .filter(lift -> id.toString().equals(lift.get(SkiLiftsListEnum.ID)))
                .findAny()
                .orElse(null);
        return skiLift == null ? "" : skiLift.get(SkiLiftsListEnum.NAME);
    }
    protected String getSkiLiftPoints(Integer id) {
        EnumMap<SkiLiftsListEnum, String> skiLift = skiLiftsList.stream()
                .filter(lift -> id.toString().equals(lift.get(SkiLiftsListEnum.ID)))
                .findAny()
                .orElse(null);
        return skiLift == null ? "" : skiLift.get(SkiLiftsListEnum.POINTS);

    }
    protected String getSkiLiftState(Integer id) {
        EnumMap<SkiLiftsListEnum, String> skiLift = skiLiftsList.stream()
                .filter(lift -> id.toString().equals(lift.get(SkiLiftsListEnum.ID)))
                .findAny()
                .orElse(null);
        return skiLift == null ? "" : skiLift.get(SkiLiftsListEnum.STATE);
    }


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

