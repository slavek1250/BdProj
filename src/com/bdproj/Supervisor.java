package com.bdproj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;

public class Supervisor {
    protected SystemUser systemUser;
    protected PriceList priceList;
    protected EmployeeAdmin employeeAdmin;
    protected SkiLiftAdmin skiLiftAdmin;

    private String lastError;

    protected enum SupervisorsListEnum { ID, NAME, SURNAME };
    protected ArrayList<EnumMap<SupervisorsListEnum, String>> supervisorsList = null;
    protected enum EmployeeListEnum { ID, NAME, SURNAME };
    protected ArrayList<EnumMap<EmployeeListEnum, String>> employeeList=null;
//    protected ArrayList<Pair<Integer,Pair<String,String>>> employeeList=null;

    public Supervisor(SystemUser user) {
        systemUser = user;
        priceList = new PriceList(user);
        employeeAdmin = new EmployeeAdmin(user);
        skiLiftAdmin = new SkiLiftAdmin(user);
        //reports = new Reports(user);
    }


    protected Integer getSupervisorId(String name, String surname) {
        EnumMap<SupervisorsListEnum, String> supervisor = supervisorsList.stream()
                .filter(sv -> (name.equals(sv.get(SupervisorsListEnum.NAME)) && surname.equals(sv.get(SupervisorsListEnum.SURNAME))))
                .findAny()
                .orElse(null);
        return supervisor == null ? -1 : Integer.parseInt(supervisor.get(SupervisorsListEnum.ID));
    }

    protected String getSupervisorName(Integer id) {
        EnumMap<SupervisorsListEnum, String> supervisor = supervisorsList.stream()
                .filter(sv -> id.toString().equals(sv.get(SupervisorsListEnum.ID)))
                .findAny()
                .orElse(null);
        return supervisor == null ? "" : supervisor.get(SupervisorsListEnum.NAME);
    }

    protected String getSupervisorSurname(Integer id) {
        EnumMap<SupervisorsListEnum, String> supervisor = supervisorsList.stream()
                .filter(sv -> id.toString().equals(sv.get(SupervisorsListEnum.ID)))
                .findAny()
                .orElse(null);
        return supervisor == null ? "" : supervisor.get(SupervisorsListEnum.SURNAME);
    }

    public String getLastError() {
        return lastError;
    }


    protected void addSupervisor(Integer id, String name, String surname) {
        EnumMap<SupervisorsListEnum, String> tmp = new EnumMap<>(SupervisorsListEnum.class);
        tmp.put(SupervisorsListEnum.ID, id.toString());
        tmp.put(SupervisorsListEnum.NAME, name);
        tmp.put(SupervisorsListEnum.SURNAME, surname);
        supervisorsList.add(tmp);
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
                EnumMap<SupervisorsListEnum, String> tmp = new EnumMap<>(SupervisorsListEnum.class);
                tmp.put(SupervisorsListEnum.ID, rs.getString("id"));
                tmp.put(SupervisorsListEnum.NAME, rs.getString("imie"));
                tmp.put(SupervisorsListEnum.SURNAME, rs.getString("nazwisko"));
                supervisorsList.add(tmp);
            }
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }


    protected String getEmployeeName(Integer id) {
        return employeeList
                .stream()
                .filter(sv -> id.equals(Integer.parseInt(sv.get(EmployeeListEnum.ID))))
                .findAny()
                .map(sv -> sv.get(EmployeeListEnum.NAME))
                .orElse("");
    }

    protected String getEmployeeSurname(Integer id) {
        return employeeList
                .stream()
                .filter(sv -> id.equals(Integer.parseInt(sv.get(EmployeeListEnum.ID))))
                .findAny()
                .map(sv -> sv.get(EmployeeListEnum.SURNAME))
                .orElse("");
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
                    EnumMap<EmployeeListEnum, String> tmp = new EnumMap<>(EmployeeListEnum.class);
                    tmp.put(EmployeeListEnum.ID, rs.getString("id"));
                    tmp.put(EmployeeListEnum.NAME, rs.getString("imie"));
                    tmp.put(EmployeeListEnum.SURNAME, rs.getString("nazwisko"));
                    employeeList.add(tmp);
                }
                return true;
            } catch (SQLException e) {
                lastError=e.getMessage();
            }
        }
        return false;
    }
}
