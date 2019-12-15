package com.bdproj;

import javafx.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Supervisor {
    protected SystemUser systemUser;
    protected PriceList priceList;
    protected EmployeeAdmin employeeAdmin;
    protected SkiLiftAdmin skiLiftAdmin;
    protected Reports reports;

    private String lastError;

    protected ArrayList<Pair<Integer, Pair<String, String>>> supervisorsList = null;
    protected ArrayList<Pair<Integer, String>> skiLiftsList = null;
    protected ArrayList<Pair<Integer,Pair<String,String>>> employeeList=null;

    public Supervisor(SystemUser user) {
        systemUser = user;
        priceList = new PriceList(user);
        employeeAdmin = new EmployeeAdmin(user);
        skiLiftAdmin = new SkiLiftAdmin(user);
        reports = new Reports(user);
    }

    protected Integer getSkiLiftId(String name) {
        Pair<Integer, String> skiLift = skiLiftsList.stream()
                .filter(lift -> name.equals(lift.getValue()))
                .findAny()
                .orElse(null);
        return skiLift == null ? -1 : skiLift.getKey();
    }

    protected String getSkiLiftName(Integer id) {
        Pair<Integer, String> skiLift = skiLiftsList.stream()
                .filter(lift -> id.equals(lift.getKey()))
                .findAny()
                .orElse(null);
        return skiLift == null ? "" : skiLift.getValue();
    }

    protected Integer getSupervisorId(String name, String surname) {
        Pair<Integer, Pair<String, String>> supervisor = supervisorsList.stream()
                .filter(sv -> (name.equals(sv.getValue().getValue()) && surname.equals(sv.getValue().getValue())))
                .findAny()
                .orElse(null);
        return supervisor == null ? -1 : supervisor.getKey();
    }

    protected String getSupervisorName(Integer id) {
        Pair<Integer, Pair<String, String>> supervisor = supervisorsList.stream()
                .filter(sv -> id.equals(sv.getKey()))
                .findAny()
                .orElse(null);
        return supervisor == null ? "" : supervisor.getValue().getValue();
    }

    protected String getSupervisorSurname(Integer id) {
        Pair<Integer, Pair<String, String>> supervisor = supervisorsList.stream()
                .filter(sv -> id.equals(sv.getKey()))
                .findAny()
                .orElse(null);
        return supervisor == null ? "" : supervisor.getValue().getKey();
    }

    public String getLastError() {
        return lastError;
    }

    protected void addSkiLift(Integer id, String name) {
        skiLiftsList.add(new Pair<>(id, name));
    }

    protected void addSupervisor(Integer id, String name, String surname) {
        supervisorsList.add(new Pair<>(id, new Pair<>(surname, name)));
    }

    protected boolean fetchSupervisors() {
        String query = "select id, nazwisko, imie from kierownik where zwolniony = 0 AND id!=?;";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1,systemUser.getId());
            ResultSet rs = ps.executeQuery();

            supervisorsList = new ArrayList<>();

            while (rs.next()) {
                Integer id = rs.getInt("id");
                String name = rs.getString("imie");
                String surname = rs.getString("nazwisko");
                supervisorsList.add(new Pair<>(id, new Pair<>(surname, name)));
            }
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    protected boolean fetchSkiLifts() {

        String query = "select w.id, w.nazwa from wyciag w\n" +
                        "where (w.id, 0) = (\n" +
                        "\t\tselect d.wyciag_id, sum(d.nieistniejacy) from wyciag_dane d\n" +
                        "\t\twhere d.wyciag_id = w.id and kierownik_id=? group by wyciag_id );";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1,systemUser.getId());
            ResultSet rs = ps.executeQuery();

            skiLiftsList = new ArrayList<>();

            while (rs.next()) {
                Integer id = rs.getInt("id");
                String name = rs.getString("nazwa");
                skiLiftsList.add(new Pair<>(id, name));
            }
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }
    protected String getEmployeeName(Integer id) {
        Pair<Integer, Pair<String, String>> supervisor = employeeList.stream()
                .filter(sv -> id.equals(sv.getKey()))
                .findAny()
                .orElse(null);
        return supervisor == null ? "" : supervisor.getValue().getValue();
    }

    protected String getEmployeeSurname(Integer id) {
        Pair<Integer, Pair<String, String>> supervisor = employeeList.stream()
                .filter(sv -> id.equals(sv.getKey()))
                .findAny()
                .orElse(null);
        return supervisor == null ? "" : supervisor.getValue().getKey();
    }
    public boolean fetchEmployees(){

        String query="select id, nazwisko, imie from pracownicy where zwolniony = 0 and kierownik_id=? ";
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        if(MySQLConnection.prepareConnection()) {
            try {
               PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
                ps.setInt(1,systemUser.getId());
               ResultSet rs = ps.executeQuery();
                employeeList =new ArrayList<>();
                while (rs.next()) {
                    int id=rs.getInt("id");
                    String surname=rs.getString("nazwisko");
                    String name=rs.getString("imie");
                    employeeList.add(new Pair<>(id, new Pair<>(surname, name)));
                }
                return true;
            } catch (SQLException e) {
                lastError=e.getMessage();
            }
        }
        return false;
    }
}
