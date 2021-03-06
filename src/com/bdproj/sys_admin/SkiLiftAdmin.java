package com.bdproj.sys_admin;
import com.bdproj.db.MySQLConnection;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Klasa służąca do obsługi wyciągów oraz ich modyfikowania.
 */
public class SkiLiftAdmin {
    private SystemUser systemUser;  /**< Obiekt obecnie zalogowanego użytkownika systemu. */
    public enum SkiLiftsListEnum { ID, NAME, POINTS, STATE }; /** < Dane dotyczące wyciągu. */
    public ArrayList<EnumMap<SkiLiftsListEnum, String>> skiLiftsList = null; /** < Obiekt do przechowywania listy wyciągów. */
    private String lastError; /**< Opis ostatniego błędu. */
    /**
     * Metoda zapisująca dane obecnie zalogowanego kierownika.
     * @param user Obiekt zawierający dane obecnie zalogowanego użytkownika.
     */
    public SkiLiftAdmin(SystemUser user) { systemUser = user; }
    /**
     * Metoda odpowiedzialna za pobranie listy wyciągów którymi zarządza obecnie zalogowany kierownik.
     * @return Zwraca true jeżeli operacja zakończyła się sukcesem.
     */
    public boolean fetchSkiLifts() {

        String query = "select w.id, w.nazwa, wd.koszt_pkt, wd.stan\n" +
                "from wyciag w join wyciag_dane wd on w.id = wd.wyciag_id join zarzadcy z on w.id = z.wyciag_id\n" +
                "where wd.id = ( select wd2.id from wyciag_dane wd2 where wd2.wyciag_id = w.id order by wd2.od desc limit 1)\n" +
                "and z.do is null and wd.nieistniejacy = 0 and z.kierownik_id = ?";

        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }

        try {
            PreparedStatement ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1, systemUser.getId());
            ResultSet rs = ps.executeQuery();

            skiLiftsList = new ArrayList<>();

            while (rs.next()) {
                EnumMap<SkiLiftsListEnum, String> tmp = new EnumMap<>(SkiLiftsListEnum.class);
                tmp.put(SkiLiftsListEnum.ID, rs.getString("id"));
                tmp.put(SkiLiftsListEnum.NAME, rs.getString("nazwa"));
                tmp.put(SkiLiftsListEnum.POINTS,rs.getString("koszt_pkt"));
                tmp.put(SkiLiftsListEnum.STATE,rs.getString("stan"));
                skiLiftsList.add(tmp);
            }
            return true;
        }
        catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return false;
    }

    /**
     * Getter.
     * @return Zwarca opis ostatniego błędu.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Getter.
     * @param id Numer id wyciągu.
     * @return Zwraca nazwę wyciągu.
     */
    protected String getSkiLiftName(Integer id) {
        EnumMap<SkiLiftsListEnum, String> skiLift = skiLiftsList.stream()
                .filter(lift -> id.toString().equals(lift.get(SkiLiftsListEnum.ID)))
                .findAny()
                .orElse(null);
        return skiLift == null ? "" : skiLift.get(SkiLiftsListEnum.NAME);
    }

    /**
     * Getter.
     * @param id Numer id wyciągu.
     * @return Zwraca koszt punkotwy wyciągu.
     */
    protected String getSkiLiftPoints(Integer id) {
        EnumMap<SkiLiftsListEnum, String> skiLift = skiLiftsList.stream()
                .filter(lift -> id.toString().equals(lift.get(SkiLiftsListEnum.ID)))
                .findAny()
                .orElse(null);
        return skiLift == null ? "" : skiLift.get(SkiLiftsListEnum.POINTS);

    }

    /**
     * Getter.
     * @param id Numer id wyciągu.
     * @return Zwraca stan wyciągu.
     */
    protected String getSkiLiftState(Integer id) {
        EnumMap<SkiLiftsListEnum, String> skiLift = skiLiftsList.stream()
                .filter(lift -> id.toString().equals(lift.get(SkiLiftsListEnum.ID)))
                .findAny()
                .orElse(null);
        return skiLift == null ? "" : skiLift.get(SkiLiftsListEnum.STATE);
    }

    /**
     * Metoda dodająca nowy wyciąg.
     * @param name Nazwa nowego wyciągu podana przez użytkownika.
     * @param height Wysokość wyciągu.
     * @param pointsCost Koszt punkotwy wyciągu.
     * @param state Stan wyciągu (w użytku/zamknięty)
     * @param idSup id kierownika.
     */
    public void addNewLift(String name, String height, String pointsCost, boolean state, int idSup){
        PreparedStatement ps1;
        PreparedStatement ps2;
        PreparedStatement ps3;
        ResultSet rs;
        int idLift = 0;

        String sql_1 = "INSERT INTO wyciag (nazwa, wysokosc) VALUES(?,?)";
        String sql_2 = "INSERT INTO wyciag_dane (od, koszt_pkt, stan, wyciag_id, kierownik_id) VALUES(now(),?,?,?,?)";
        String sql_3 = "INSERT INTO zarzadcy (od, `do`, kierownik_id, wyciag_id) VALUES (NOW(), NULL, ?,?)";

        if(MySQLConnection.prepareConnection()) {
            try {
                ps1 = MySQLConnection.getConnection().prepareStatement(sql_1, Statement.RETURN_GENERATED_KEYS);
                ps1.setString(1, name);
                ps1.setString(2, height);
                ps1.executeUpdate();
                rs = ps1.getGeneratedKeys();
                if(rs.first()){
                    idLift = rs.getInt(1);
                }
                ps3 = MySQLConnection.getConnection().prepareStatement(sql_3);
                ps3.setInt(1,systemUser.getId());
                ps3.setInt(2,idLift);
                ps3.executeUpdate();
                ps2 = MySQLConnection.getConnection().prepareStatement(sql_2);
                ps2.setString(1, pointsCost);
                ps2.setBoolean(2, state);
                ps2.setInt(3, idLift);
                ps2.setInt(4,idSup);
                ps2.executeUpdate();
            } catch (SQLException e) {
                lastError = e.getMessage();
            }
        }
    }

    /**
     * Metoda obsługująca mianowanie innego kierownika na zarządcę wyciągu.
     * @param supId id mianowanego kierownika.
     * @param liftId id wyciągu.
     * @return Zwarca false jeżeli kierownik jest już zarządcą wybranego wyciągu.
     */
    public boolean promoteNewLiftSupervisor(int supId,int liftId){
        String query="SELECT * from zarzadcy WHERE  kierownik_id=? AND wyciag_id=? AND do IS NULL";
        String query1="INSERT INTO zarzadcy (od,do,kierownik_id,wyciag_id) VALUES(now(),NULL,?,?)";
        PreparedStatement ps,ps1;
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }
            try{
                ps=MySQLConnection.getConnection().prepareStatement(query);
                ps.setInt(1,supId);
                ps.setInt(2,liftId);
                ResultSet rs=ps.executeQuery();
                if(rs.first()){
                    JOptionPane.showMessageDialog(null,"Wybrany kierownik jest już zarządzcą wyciągu '"+getSkiLiftName(liftId)+"'");
                    return false;
                }
               ps1=MySQLConnection.getConnection().prepareStatement(query1);
                ps1.setInt(1,supId);
                ps1.setInt(2,liftId);
                ps1.executeUpdate();
                return true;
            }catch (SQLException e){
                lastError = e.getMessage();
            }
        return true;
    }

    /**
     * Metoda odpowiedzialna za usuwanie swojego prawa do zarządzania wyciągiem.
     * @param liftId id wyciągu.
     * @return Zwaraca false jeżeli użytkownik jest jedynym zarządcą danego wyciagu.
     */
    public boolean quitManagingLift(int liftId){
    String query="SELECT * FROM zarzadcy WHERE kierownik_id!=? AND wyciag_id=? AND do IS NULL";
    String query1="UPDATE zarzadcy SET do=now() WHERE wyciag_id=? AND kierownik_id=? AND do IS NULL";
    PreparedStatement ps,ps1;
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
            return false;
        }
        try{
            ps=MySQLConnection.getConnection().prepareStatement(query);
            ps.setInt(1,systemUser.getId());
            ps.setInt(2,liftId);
            ResultSet rs=ps.executeQuery();
            if(!rs.next()){
                JOptionPane.showMessageDialog(null,"Nie możesz usunąć swoich praw do zarządzania, ponieważ jesteś jedynym zarządzcą wyciągu: "+getSkiLiftName(liftId));
                return false;
            }
            ps1=MySQLConnection.getConnection().prepareStatement(query1);
            ps1.setInt(1,liftId);
            ps1.setInt(2,systemUser.getId());
            ps1.executeUpdate();
            return true;
        }catch(SQLException e){
            lastError = e.getMessage();
        }
        return true;
    }

    /**
     * Metoda zapisująca zmodyfikowane dane wyciągu.
     * @param points Nowy koszt punktowy wyciągu.
     * @param state Nowy stan wyciągu.
     * @param liftId Id wyciągu dla którego mają zostać dokonane zmiany.
     */
    public void saveSkiLiftChanges(String points,String state,int liftId) {
      String query=  "INSERT INTO wyciag_dane(od, koszt_pkt, stan, nieistniejacy, wyciag_id, kierownik_id) VALUES(now(), ?,?,0,?,?)";
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
        }
        try{
            PreparedStatement ps=MySQLConnection.getConnection().prepareStatement(query);
            ps.setString(1,points);
            ps.setString(2,state);
            ps.setInt(3,liftId);
            ps.setInt(4,systemUser.getId());
            ps.executeUpdate();
        }catch(SQLException e){
            lastError = e.getMessage();
        }
    }

    /**
     * Metoda usuwająca wyciąg.
     * @param liftId id wyciągu.
     */
    public void deleteSkiLift(int liftId){
        String query=  "INSERT INTO wyciag_dane (koszt_pkt,stan,wyciag_id,kierownik_id) SELECT koszt_pkt, stan, wyciag_id,kierownik_id FROM wyciag_dane WHERE id=(select id FROM wyciag_dane WHERE wyciag_id=? \n" +
                        "ORDER BY od DESC limit 1); ";
        String query1="UPDATE wyciag_dane SET od=now(),nieistniejacy=1 WHERE id=?";
        if(!MySQLConnection.prepareConnection()) {
            lastError = MySQLConnection.getLastError();
        }
        int newId=0;
        try{
            PreparedStatement ps=MySQLConnection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1,liftId);
            ps.executeUpdate();
            ResultSet rs=ps.getGeneratedKeys();
            if(rs.first()){
                newId=rs.getInt(1);
            }
            PreparedStatement ps1=MySQLConnection.getConnection().prepareStatement(query1);
            ps1.setInt(1,newId);
            ps1.executeUpdate();
        }catch(SQLException e){
            lastError = e.getMessage();
        }
    }
}

