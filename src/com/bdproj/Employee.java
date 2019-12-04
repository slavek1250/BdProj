package com.bdproj;

public class Employee {
    protected SystemUser systemUser;
    protected Tickets tickets;

    public Employee(SystemUser user) {
        systemUser = user;
        tickets = new Tickets(user);
    }
}
