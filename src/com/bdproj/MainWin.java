package com.bdproj;

//import org.jdesktop.swingx.JXDatePicker;
import org.knowm.xchart.SwingWrapper;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Dimension;
import java.awt.Toolkit;

/*
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

Proponuję następującą konwencję nazewnictwa:

    - STAŁE_Z_DUŻEJ_LITERY_SPACJA_JAKO_PODŁOGA
    - zmienneCzyMetodyZaczynamyZMałejZamiastSpacjiDuzaLitera
    - NazwyKlasItdPodobnieJakZmienneTylkoPierwszyWyrazTakżeZDużej

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!♠♣!!!!!!
 */


public class MainWin implements MainView {

    // TODO: Dopracowac wyglad GUI. #KLAUDIA# #KAROL#

    private static JFrame frame;
    private static SystemUser systemUser;
    private JButton btnLogin;
    private JPanel panelMain;
    private JTextField txtLogin;
    private JPasswordField txtPass;
    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private MainWin() {
        btnLogin.addActionListener(actionEvent -> logIn());
    }

    public void showMainView() {
        frame.setSize(300, 250);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        showAnotherPanel(panelMain);
        systemUser.logOut();
        txtLogin.setText("");
        txtPass.setText("");
    }

    private void showSupervisorView() {
        frame.setSize(470, 680);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        SupervisorWgt supervisorWgt = new SupervisorWgt(this, systemUser);
        showAnotherPanel(supervisorWgt.getPanel());
    }

    private void showEmployeeView() {
        frame.setSize(370, 400);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        EmployeeWgt employeeWgt = new EmployeeWgt(this, systemUser);
        showAnotherPanel(employeeWgt.getPanel());
    }

    private static void showAnotherPanel(JPanel toShow) {
        frame.remove(frame.getContentPane());
        frame.setContentPane(toShow);
        toShow.updateUI();
    }

    private void logIn(){

        // login xxxyyyccc
        // xxx - 3 pierwsze litery imienia
        // yyy - 3 pierwsze litery nazwiska
        // ccc - id % 1000

        // hasło min 8 znaków

        String loginRegEx = "^[a-z]{6}[0-9]{4}$";
        String login= txtLogin.getText();
        if(!login.matches(loginRegEx)) {
            JOptionPane.showMessageDialog(panelMain, "Błędny login.");
            return;
        }

        String password= String.valueOf(txtPass.getPassword());
        if(password.length() < 8) {
            JOptionPane.showMessageDialog(panelMain, "Zbyt krótkie hasło.");
            return;
        }

        SystemUser.UserType userType = systemUser.logIn(login, password);

        if(userType == SystemUser.UserType.SUPERVISOR) {
            showSupervisorView();
        }
        else if (userType == SystemUser.UserType.EMPLOYEE) {
            showEmployeeView();
        }
        else {
            JOptionPane.showMessageDialog(panelMain, "Nie jesteś użytkownikiem systemu.");
        }
    }

    public static void main(String[] args) {
        frame = new JFrame("Wyciąg Narciarski u Skoczka");
        systemUser = new SystemUser();
        MainWin mainWin = new MainWin();

        frame.setContentPane(mainWin.panelMain);
        //chartDesignHelper();

        frame.setSize(300, 250);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        if(!MySQLConnection.readConnParamsFromFile()) {
            JOptionPane.showMessageDialog(mainWin.panelMain, MySQLConnParams.getLastError());
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
    }

    public static void chartDesignHelper() {
        ReportChart ticketChart = new ReportChart("Statystyki dla wyciągów", "Nazwa wyciągu", "");
        ticketChart.addSeries("Przewyższenie", new ArrayList<String>(Arrays.asList("Czarny groń", "Mały")), new ArrayList<Integer>(Arrays.asList(1586, 400)));
        new SwingWrapper(ticketChart.getChart()).displayChart();

        ReportChart skiLiftChart = new ReportChart("Statystyki", "Godzina", "");
        skiLiftChart.addSeries("Punkty", new ArrayList<String>(Arrays.asList("13", "14")), new ArrayList<Integer>(Arrays.asList(60, 30)));
        skiLiftChart.addSeries("Kwota [zł]", new ArrayList<String>(Arrays.asList("13", "14")), new ArrayList<Double>(Arrays.asList(27.0, 13.5)));
        new SwingWrapper(skiLiftChart.getChart()).displayChart();

    }
}