package com.bdproj;

public class Supervisor {
    protected SystemUser systemUser;
    protected PriceList priceList;

    public Supervisor(SystemUser user) {
        systemUser = user;
        priceList = new PriceList(user);
    }
}
