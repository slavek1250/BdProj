package com.bdproj;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.awt.Toolkit;

public class MainWin implements MainView {

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

        frame.setSize(300, 250);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        frame.setSize(512, 300);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        if(!MySQLConnection.readConnParamsFromFile()) {
            JOptionPane.showMessageDialog(mainWin.panelMain, MySQLConnParams.getLastError());
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
    }
}