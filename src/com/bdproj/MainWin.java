package com.bdproj;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.logging.*;

/*
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

Proponuję następującą konwencję nazewnictwa:

    - STAŁE_Z_DUŻEJ_LITERY_SPACJA_JAKO_PODŁOGA
    - zmienneCzyMetodyZaczynamyZMałejZamiastSpacjiDuzaLitera
    - NazwyKlasItdPodobnieJakZmienneTylkoPierwszyWyrazTakżeZDużej

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */


public class MainWin implements MainView {

    // TODO: Dopracowac wyglad GUI.

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

        String loginRegEx = "[a-z]{6}[0-9]{4}";
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

        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}