package com.bdproj;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Klasa główna GUI.
 * @see MainView
 */
public class MainWin implements MainView {

    private static JFrame frame;            /**< Ramka do wyświetlania GUI. */
    private JPanel panelMain;               /**< Panel główny. */
    private JTextField txtLogin;            /**< Pole tekstowe loginu. */
    private JPasswordField txtPass;         /**< Pole tekstowa hasła. */
    private JButton btnLogin;               /**< Przycisk logowania. */
    private static SystemUser systemUser;   /**< Obiekt użytkownika systemu. */
    /**
     * Wymiary okna.
     */
    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * Domyślny konstruktor.
     */
    private MainWin() {
        btnLogin.addActionListener(actionEvent -> logIn());
    }

    /**
     * Metoda służąca do wyświetlania widoku głównego (ekranu logowania).
     * @see MainView
     */
    @Override
    public void showMainView() {
        frame.setSize(300, 250);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        showAnotherPanel(panelMain);
        systemUser.logOut();
        txtLogin.setText("");
        txtPass.setText("");
    }

    /**
     * Metoda wyświetlająca widok kierownika.
     */
    private void showSupervisorView() {
        frame.setSize(470, 680);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        SupervisorWgt supervisorWgt = new SupervisorWgt(this, systemUser);
        showAnotherPanel(supervisorWgt.getPanel());
    }

    /**
     * Metoda wyświetlająca widok pracownika.
     */
    private void showEmployeeView() {
        frame.setSize(370, 400);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        EmployeeWgt employeeWgt = new EmployeeWgt(this, systemUser);
        showAnotherPanel(employeeWgt.getPanel());
    }

    /**
     * Metoda wyświelająca zadany panel.
     * @param toShow Panel do wyświetlenia w ramce.
     */
    private static void showAnotherPanel(JPanel toShow) {
        frame.remove(frame.getContentPane());
        frame.setContentPane(toShow);
        toShow.updateUI();
    }

    /**
     * Metoda odpowiedzialna za logowanie.
     *  - Sprawdza czy użytkownik istnieje.
     *  - Wyświetla odpowiedni widok w zależności do której grupy należy użytkownik.
     */
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

    /**
     * Główna metoda programu. Punkt wejściowy.
     * @param args Argumenty uruchomieniowe programu.
     */
    public static void main(String[] args) {
        System.getProperty("file.encoding","UTF-8");
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

        ImageIcon img = new ImageIcon("skiLift.ico");
        frame.setIconImage(img.getImage());

        if(!MySQLConnection.readConnParamsFromFile()) {
            JOptionPane.showMessageDialog(mainWin.panelMain, MySQLConnParams.getLastError());
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
    }
}