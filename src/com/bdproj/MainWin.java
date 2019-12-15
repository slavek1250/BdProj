package com.bdproj;

import org.knowm.xchart.SwingWrapper;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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

    private MainWin() {
        btnLogin.addActionListener(actionEvent -> logIn());
    }

    public void showMainView() {
        showAnotherPanel(panelMain);
        systemUser.logOut();
        txtLogin.setText("");
        txtPass.setText("");
    }

    private void showSupervisorView() {
        SupervisorWgt supervisorWgt = new SupervisorWgt(this, systemUser);
        showAnotherPanel(supervisorWgt.getPanel());
    }

    private void showEmployeeView() {
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

        chartDesignHelper();

        frame.setSize(700, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void chartDesignHelper() {
        ReportChart ticketChart = new ReportChart("Statystyki dla wyciągów", "Nazwa wyciągu", "");
        ticketChart.addSeries("Przewyższenie", new ArrayList<String>(Arrays.asList(new String[] {"Czarny groń", "Mały"})), new ArrayList<Integer>(Arrays.asList(new Integer[]{1586, 400})));
        new SwingWrapper(ticketChart.getChart()).displayChart();

        ReportChart skiLiftChart = new ReportChart("Statystyki", "Godzina", "");
        skiLiftChart.addSeries("Punkty", new ArrayList<String>(Arrays.asList(new String[] {"13", "14"})), new ArrayList<Integer>(Arrays.asList(new Integer[]{60, 30})));
        skiLiftChart.addSeries("Kwota [zł]", new ArrayList<String>(Arrays.asList(new String[] {"13", "14"})), new ArrayList<Double>(Arrays.asList(new Double[]{27.0, 13.5})));
        new SwingWrapper(skiLiftChart.getChart()).displayChart();

    }
}