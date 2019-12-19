package com.bdproj;

import com.mysql.jdbc.authentication.MysqlClearPasswordPlugin;
import org.knowm.xchart.style.markers.Square;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;


public class Tickets {

    private String lastError;
    private SystemUser systemUser;
    // TODO: Generowanie nowego biletu, generacja id. #KLAUDIA#
    // TODO: Pobieranie bierzacego cennika. #KLAUDIA#
    // TODO: Doladowywanie biletu. #KLAUDIA#
    // TODO: Blokowanie biletow (ustawienie flagi).#Karol# !!DONE!!

    private enum PriceListEnum { ID_PRICE_LIST_ITEM, ID_PRICE_LIST_DICTIONARY, NAME, PRICE };
    private ArrayList<EnumMap<PriceListEnum, String>> currentPriceList;



    public String getLastError() {
        return lastError;
    }

    public boolean fetchCurrentPriceList() {
        String query =
                "select  pc.id as 'poz_cennik_id', sc.id as 'slownik_cennik_id', sc.nazwa, pc.cena\n" +
                "from poz_cennik pc join slownik_cennik sc on pc.slownik_cennik_id = sc.id\n" +
                "where pc.cennik_id = (select c.id from cennik c where c.od < now() and c.odw_przed_wej=0 order by c.od desc limit 1);";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            currentPriceList = new ArrayList<>();
            while (rs.next()) {
                EnumMap<PriceListEnum, String> item = new EnumMap<>(PriceListEnum.class);
                item.put(PriceListEnum.ID_PRICE_LIST_ITEM, rs.getString("poz_cennik_id"));
                item.put(PriceListEnum.ID_PRICE_LIST_DICTIONARY, rs.getString("slownik_cennik_id"));
                item.put(PriceListEnum.NAME, rs.getString("nazwa"));
                item.put(PriceListEnum.PRICE, rs.getString("cena"));
                currentPriceList.add(item);
            }
            if(currentPriceList.isEmpty()) {
                lastError = "Brak zdefiniowanego cennika.";
            }
            else return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }


    public ArrayList<String> getPriceListItem(){
        /*
        PreparedStatement ps;
        ResultSet rs;
        String query="SELECT CONCAT(sc.id,'. ', sc.nazwa) AS 'cennik' FROM slownik_cennik sc";
        ArrayList<String> prc =new ArrayList<String>();
        if(MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int i = 1;
                    prc.add(rs.getString(i));
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

         */
        ArrayList<String> prc = new ArrayList<String>();
        currentPriceList
                .stream()
                .map(item -> (
                        item.get(PriceListEnum.ID_PRICE_LIST_DICTIONARY) + ". " + item.get(PriceListEnum.NAME)
                ))
                .forEach(prc::add);
        return prc;
    }

    public Double getPrice(Integer dictionaryId) {
        EnumMap<PriceListEnum, String> priceListItem = currentPriceList
                .stream()
                .filter(
                        item -> item.get(PriceListEnum.ID_PRICE_LIST_DICTIONARY).equals(dictionaryId.toString()
                ))
                .findAny()
                .orElse(null);
        return (priceListItem == null ? 0.0 : Double.parseDouble(priceListItem.get(PriceListEnum.PRICE)));
    }

    public Integer getPriceListItemId(Integer dictionaryId) {
        EnumMap<PriceListEnum, String> priceListItem = currentPriceList
                .stream()
                .filter(
                        item -> item.get(PriceListEnum.ID_PRICE_LIST_DICTIONARY).equals(dictionaryId.toString()
                        ))
                .findAny()
                .orElse(null);
        return (priceListItem == null ? 0 : Integer.parseInt(priceListItem.get(PriceListEnum.ID_PRICE_LIST_ITEM)));
    }

    public int newTicket(String points, int priceListItemId){
        PreparedStatement ps1, ps2;
        int ticketNumber = 0;
        String query1 = "INSERT INTO karnet (zablokowany) VALUES ('0')";
        String query2 = "INSERT INTO hist_dolad (l_pkt, stempelczasowy, karnet_id, pracownicy_id, poz_cennik_id) VALUES (?,now(),?,?,?)";
        if(MySQLConnection.prepareConnection()){
            try{
                ps1 = MySQLConnection.getConnection().prepareStatement(query1, Statement.RETURN_GENERATED_KEYS);
                ps1.executeUpdate();
                ResultSet rs=ps1.getGeneratedKeys();
                if (rs.next()){
                    ticketNumber = rs.getInt(1);
                }
                ps2 = MySQLConnection.getConnection().prepareStatement(query2);
                ps2.setString(1,points);
                ps2.setInt(2,ticketNumber);
                ps2.setInt(3,systemUser.getId());
                ps2.setInt(4,priceListItemId);
                ps2.executeUpdate();

            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return ticketNumber;
    }

    public boolean checkTicketParameters(String ticketId){
        PreparedStatement ps;
        String query="SELECT * FROM karnet WHERE id=? AND zablokowany=0";
        try {
            ps=MySQLConnection.getConnection().prepareStatement(query);
            ps.setString(1,ticketId);
            ResultSet rs1=ps.executeQuery();
            if(rs1.first())
            {
               return true;
            }else{return false;}
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
    public int newTopUpTicket(String ticketId, String points, int priceListItemId){
        PreparedStatement ps1, ps2;
        ResultSet rs;
        int amountOfPoints = 0;
        String query1 = "INSERT INTO hist_dolad (l_pkt, stempelczasowy, karnet_id, pracownicy_id, poz_cennik_id) VALUES (?,now(),?,?,?)";
        String query2 = "SELECT sum(l_pkt) as 'suma' FROM hist_dolad hd WHERE hd.karnet_id = ?";
        if(MySQLConnection.prepareConnection()){
            try{

                ps1 = MySQLConnection.getConnection().prepareStatement(query1);
                ps1.setString(1,points);
                ps1.setString(2,ticketId);
                ps1.setInt(3,systemUser.getId());
                ps1.setInt(4,priceListItemId);
                ps1.executeUpdate();
                ps2 = MySQLConnection.getConnection().prepareStatement(query2);
                ps2.setString(1,ticketId);
                rs = ps2.executeQuery();
                if(rs.first()){
                    amountOfPoints = rs.getInt("suma");
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }return amountOfPoints;
    }

    public String ticketNoIncrement () {
        PreparedStatement ps;
        ResultSet rs;
        String out="0";
        String query = "SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = \"slavek_bd2\" AND TABLE_NAME = \"karnet\"";
        if(MySQLConnection.prepareConnection()) {
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                rs = ps.executeQuery();

                if (rs.first()) {
                    out = (rs.getString(1));
                } else {
                    out = "W bazie nie ma żadnych biletów";
                }

            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return out;
    }



public void blockTicket (String ticketnumber){
    PreparedStatement ps;
    ResultSet rs;
    String query ="SELECT zablokowany FROM karnet WHERE id=?";
    if(MySQLConnection.prepareConnection()) {
        try {
            ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setString(1, ticketnumber);
            rs = ps.executeQuery();
            if (rs.first()) {
                int zab = rs.getInt("zablokowany");
                if (zab == 1) {
                    JOptionPane.showMessageDialog(null, "Ten bilet jest już zablokowany");
                } else {
                    String query1 = "UPDATE karnet SET zablokowany=1 WHERE id=?";
                    PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1);
                    ps1.setString(1, ticketnumber);
                    int rs1 = ps1.executeUpdate();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Nie ma takiego biletu");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

    public Tickets(SystemUser user) {
        systemUser = user;
    }
}