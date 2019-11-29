package com.bdproj;

public class SystemUser {

    public enum UserType {
        UNREGISTERED,
        EMPLOYEE,
        SUPERVISOR
    }

    private String name, surname;
    private int id;
    private UserType userType;

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getId() {
        return id;
    }

    public UserType getUserType() {
        return userType;
    }

    public UserType logIn() {


        return UserType.UNREGISTERED;
    }

}
