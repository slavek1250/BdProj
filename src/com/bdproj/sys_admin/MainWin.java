package com.bdproj.sys_admin;

import com.bdproj.db.MySQLConnParams;
import com.bdproj.db.MySQLConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

        frame.setSize(300, 250);
    }

    /**
     * Metoda służąca do wyświetlania widoku głównego (ekranu logowania).
     * @see MainView
     */
    @Override
    public void showMainView() {
        //frame.setSize(300, 250);
        //frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        showAnotherPanel(panelMain);
        systemUser.logOut();
        txtLogin.setText("");
        txtPass.setText("");
    }

    /**
     * Metoda wyświetlająca widok kierownika.
     */
    private void showSupervisorView() {
       // frame.setSize(470, 680);
        //frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        SupervisorWgt supervisorWgt = new SupervisorWgt(this, systemUser);
        showAnotherPanel(supervisorWgt.getPanel());
    }

    /**
     * Metoda wyświetlająca widok pracownika.
     */
    private void showEmployeeView() {
        //frame.setSize(370, 400);
        //frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
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
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.repaint();
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
            JOptionPane.showMessageDialog(panelMain, systemUser.getLastError());
        }
    }

    /**
     * Główna metoda programu. Punkt wejściowy.
     * @param args Argumenty uruchomieniowe programu.
     */
    public static void main(String[] args) {

        frame = new JFrame("Wyciąg Narciarski u Skoczka");
        systemUser = new SystemUser();
        MainWin mainWin = new MainWin();
        showAnotherPanel(mainWin.panelMain);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("Pomoc");
        JMenuItem help = new JMenuItem("Pomoc");
        help.addActionListener(actionEvent -> showHelp());
        help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menu.add(help);
        menubar.add(menu);
        frame.setJMenuBar(menubar);

        try {
            BufferedImage img = ImageIO.read(new File("media/skiLift.ico"));
            frame.setIconImage(img);
        }
        catch (IOException ignored) {}

        frame.setVisible(true);

        if(!MySQLConnection.readConnParamsFromFile()) {
            JOptionPane.showMessageDialog(mainWin.panelMain, MySQLConnParams.getLastError());
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }

        /*
        try {
            System.setProperty("file.encoding", "UTF-8");
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);
        }
        catch (NoSuchFieldException | IllegalAccessException ignored) {}
        */
    }

    /**
     * Metoda wyświetlająca pomoc użytkownika.
     */
    public static void showHelp() {
        try {
            Runtime.getRuntime().exec("hh.exe help/UserManual.chm");
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Niestety, nie można otworzyć pliku pomocy.");
        }
    }
}