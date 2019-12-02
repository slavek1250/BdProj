package com.bdproj;

import javax.print.DocFlavor;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PriceList {
    private SystemUser systemUser;
    private String lastError;

    private ArrayList<Integer> priceListIds;
    private ArrayList<String> priceListNames;
    private ArrayList<Double> priceListPrices;
    private Integer priceListId;

    PriceList(SystemUser user) {
        systemUser = user;
    }

    public boolean fetchPriceList() {


        PreparedStatement ps;
        ResultSet rs;
        String query = "select pc.cennik_id, sc.id as slownik_cennik_id, sc.nazwa, pc.cena \n" +
                        "from poz_cennik pc right outer join slownik_cennik sc on pc.slownik_cennik_id = sc.id \n" +
                        "where not exists ( select c.id from cennik c where pc.cennik_id < any (\n" +
                            "\tselect c1.id from cennik c1) \n" +
                        ");";

        if (!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            ps = MySQLConnection.getConnection().prepareStatement(query);
            rs = ps.executeQuery();

            priceListIds = new ArrayList<Integer>();
            priceListNames = new ArrayList<String>();
            priceListPrices = new ArrayList<Double>();

            while (rs.next()) {
                priceListId = rs.getInt("cennik_id");
                priceListIds.add(rs.getInt("slownik_cennik_id"));
                priceListNames.add(rs.getString("nazwa"));
                Double price = rs.getDouble("cena");
                priceListPrices.add(rs.wasNull() ? -1 : price);
            }
            if(priceListIds.size() != 0) {
                return true;
            }
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    public ArrayList<Integer> getPriceListIds() {
        return priceListIds;
    }

    public ArrayList<String> getPriceListNames() {
        return priceListNames;
    }

    public ArrayList<Double> getPriceListPrices() {
        return priceListPrices;
    }

    public String getLastError() {
        return lastError;
    }

    public void setPriceListPrices(ArrayList<Double> priceListPrices) {
        this.priceListPrices = priceListPrices;
    }

    public boolean createNewPriceList() {

        PreparedStatement ps;
        ResultSet rs;
        String query1 = "insert into cennik set od=now(), kierownik_id=?;";
        String query2 = "select max(id) as cennik_id from cennik;";
        String query3 = "insert into poz_cennik(cena, cennik_id, slownik_cennik_id) values(?, ?, ?);";

        if (!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            ps = MySQLConnection.getConnection().prepareStatement(query1);
            ps.setInt(1, systemUser.getId());
            ps.execute();
            rs = ps.executeQuery(query2);

            if(rs.first()) {
                this.priceListId = rs.getInt("cennik_id");

                ps = MySQLConnection.getConnection().prepareStatement(query3);

                for (int i = 0; i < priceListIds.size(); i++) {
                    ps.setDouble(1, priceListPrices.get(i));
                    ps.setInt(2, priceListId);
                    ps.setInt(3, priceListIds.get(i));
                    ps.addBatch();
                }

                ps.executeBatch();
                return true;
            }
            lastError = "Can not get new price list id.";
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }
}
