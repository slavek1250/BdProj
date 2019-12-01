package com.bdproj;

public class Supervisor {
    protected SystemUser systemUser;
    protected PriceList priceList;
    protected EmployeeAdmin employeeAdmin;
    protected SkiLiftAdmin skiLiftAdmin;
    protected Reports reports;

    public Supervisor(SystemUser user) {
        systemUser = user;
        priceList = new PriceList(user);
        employeeAdmin = new EmployeeAdmin(user);
        skiLiftAdmin = new SkiLiftAdmin(user);
        reports = new Reports();
    }
}
