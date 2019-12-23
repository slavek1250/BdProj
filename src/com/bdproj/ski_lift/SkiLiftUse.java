package com.bdproj.ski_lift;

import com.bdproj.MySQLConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;

public class SkiLiftUse {

    protected enum SkiLiftsListEnum { SKI_LIFT_ID, SKI_LIFT_DATA_ID, NAME, POINTS };
    protected ArrayList<EnumMap<SkiLiftsListEnum, String>> skiLiftsList;

    private String lastError;

    protected boolean fetchSkiLifts() {
        String query =  "select w.id, wd.id as wyciag_dane_id, w.nazwa, wd.koszt_pkt\n" +
                        "from wyciag w join wyciag_dane wd on w.id = wd.wyciag_id\n" +
                        "where wd.id = ( select wd2.id from wyciag_dane wd2 where wd2.wyciag_id = w.id order by wd2.od desc limit 1 )\n" +
                        "and wd.nieistniejacy = 0 and wd.stan = 1";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            skiLiftsList = new ArrayList<>();

            while (rs.next()) {
                EnumMap<SkiLiftsListEnum, String> tmp = new EnumMap<>(SkiLiftsListEnum.class);
                tmp.put(SkiLiftsListEnum.SKI_LIFT_ID, rs.getString("id"));
                tmp.put(SkiLiftsListEnum.SKI_LIFT_DATA_ID, rs.getString("wyciag_dane_id"));
                tmp.put(SkiLiftsListEnum.NAME, rs.getString("nazwa"));
                tmp.put(SkiLiftsListEnum.POINTS,rs.getString("koszt_pkt"));
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

    protected EnumMap<SkiLiftsListEnum, String> getSkiLiftData(Integer skiLiftId) {
        return skiLiftsList
                .stream()
                .filter(
                        lift -> lift.get(SkiLiftsListEnum.SKI_LIFT_ID).equals(skiLiftId.toString())
                )
                .findAny()
                .orElse(null);
    }

    protected Integer getSkiLiftPointsCost(Integer skiLiftId) {
        EnumMap<SkiLiftsListEnum, String> skiLift = getSkiLiftData(skiLiftId);
        return skiLift == null ? Integer.MAX_VALUE : Integer.parseInt(skiLift.get(SkiLiftsListEnum.POINTS));
    }

    protected String getSkiLiftName(Integer skiLiftId) {
        EnumMap<SkiLiftsListEnum, String> skiLift = getSkiLiftData(skiLiftId);
        return skiLift == null ? "" : skiLift.get(SkiLiftsListEnum.NAME);
    }

    protected Integer getSkiLiftDataId(Integer skiLiftId) {
        EnumMap<SkiLiftsListEnum, String> skiLift = getSkiLiftData(skiLiftId);
        return skiLift == null ? -1 : Integer.parseInt(skiLift.get(SkiLiftsListEnum.SKI_LIFT_DATA_ID));
    }

    protected boolean isTicketExisting(Integer ticketId) {
        String query = "select k.id from karnet k where k.id = ? and k.zablokowany = 0;";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();

            return rs.next();
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    protected Integer getTicketPointsAmount(Integer ticketId) {
        String query =
                "select (case when kupione is null then 0 else kupione end) - (case when wydane is null then 0 else wydane end) as l_pkt\n" +
                    "from ( select ( select sum(hd.l_pkt) from hist_dolad hd where hd.karnet_id = k.id) as kupione,\n" +
                    "(select sum(wd.koszt_pkt) from uzycia_karnetu uk join wyciag_dane wd on uk.wyciag_dane_id = wd.id where uk.karnet_id = k.id) as wydane\n" +
                "from karnet k where k.id = ? ) as tmp;";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return 0;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("l_pkt");
            }
            return 0;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return 0;
    }

    protected boolean addTicketUse(Integer skiLiftDataId, Integer ticketId) {
        String query = "insert into uzycia_karnetu(wyciag_dane_id, karnet_id) values(?, ?);";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, skiLiftDataId);
            ps.setInt(2, ticketId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();

            return rs.next();
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }
}
