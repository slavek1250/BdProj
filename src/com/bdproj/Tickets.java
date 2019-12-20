package com.bdproj;
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
    // TODO: Generowanie nowego biletu, generacja id. #KLAUDIA# !!DONE!!
    // TODO: Pobieranie bierzacego cennika. #KLAUDIA# !!DONE!!
    // TODO: Doladowywanie biletu. #KLAUDIA# !!DONE!!
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
        catch (SQLException e) {lastError = e.getMessage();}
        return false;
    }


    public ArrayList<String> getPriceListItem(){
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

            }catch (SQLException e){lastError=e.getMessage(); }
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
        } catch (SQLException e) {lastError=e.getMessage();}

        return true;
    }
    public int newTopUpTicket(String ticketId, String points, int priceListItemId){
        PreparedStatement ps1, ps2,ps3;
        ResultSet rs,rs2;
        int amountOfPoints = 0,currentPoints=0,lostPoints=0;
        String query1 = "INSERT INTO hist_dolad (l_pkt, stempelczasowy, karnet_id, pracownicy_id, poz_cennik_id) VALUES (?,now(),?,?,?)";
        String query2 = "(SELECT sum(l_pkt) as 'suma' FROM hist_dolad hd WHERE hd.karnet_id = ?)";
        String query3=  "(SELECT sum(wd.koszt_pkt) FROM wyciag_dane wd JOIN uzycia_karnetu uk ON wd.id=uk.wyciag_dane_id WHERE karnet_id=?)";
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
                    currentPoints = rs.getInt("suma");
                }
                ps3=MySQLConnection.getConnection().prepareStatement(query3);
                ps3.setString(1,ticketId);
                rs2=ps3.executeQuery();
                if(rs2.first()){
                    lostPoints= rs2.getInt(1);
                }
            }catch (SQLException e){lastError=e.getMessage();}
            amountOfPoints=currentPoints-lostPoints;
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

            } catch (SQLException e) {lastError=e.getMessage();}
        }return out;
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
        } catch (SQLException e) { lastError=e.getMessage();}
    }
}
    public Tickets(SystemUser user) {
        systemUser = user;
    }
}