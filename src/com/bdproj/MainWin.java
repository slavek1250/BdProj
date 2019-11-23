package com.bdproj;

import javax.naming.NamingException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.logging.*;

public class MainWin implements MainView {

    static JFrame frame;
    static SupervisorWgt supervisorWgt;
    static EmployeeWgt employeeWgt;
    private JButton btnLogin;
    private JPanel panelMain;
    private JTextField txtLogin;
    private JPasswordField passwLogin;

    public MainWin() {

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                PreparedStatement ps;
                ResultSet rs;
                String username= txtLogin.getText();
                String password= String.valueOf(passwLogin.getPassword());
                String query= "SELECT * FROM slavek_bd2.kierownik WHERE login='karkab7028' AND haslo=MD5('test1234')";
            try {
                ps = MySQLConnection.getConnection().prepareStatement(query);
                rs = ps.executeQuery();

                if (rs.next()) {
                 JOptionPane.showMessageDialog(null, "DZIALA BAZA");
                }
                else{
                    JOptionPane.showMessageDialog(null, "COS POSZLO NIE TAK ZIOMEK");
                }
                }catch(SQLException | NamingException ex){
                    Logger.getLogger(MainWin.class.getName()).log(Level.SEVERE,null, ex);
                }
            }
        });
    }

    public boolean isSupervisor() {
        return txtLogin.getText().equals("kier");
    }

    public void showMainView(JPanel toHide) {
        frame.remove(toHide);
        frame.setContentPane(panelMain);
        panelMain.updateUI();
    }

    public void showSupervisorView() {
        frame.remove(panelMain);
        frame.setContentPane(supervisorWgt.getPanel());
        supervisorWgt.getPanel().updateUI();
    }

    public void showEmployeeView() {
        frame.remove(panelMain);
        frame.setContentPane(employeeWgt.getPanel());
        employeeWgt.getPanel().updateUI();
    }

    public static void main(String[] args) {
        frame = new JFrame("WYCIĄG NARCIARSKI U SKOCZKA");

        MainWin mainWin = new MainWin();
        employeeWgt = new EmployeeWgt((MainView)mainWin);
        supervisorWgt = new SupervisorWgt((MainView)mainWin);

        frame.setContentPane(mainWin.panelMain);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

/*btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if(isSupervisor()) {
                    showSupervisorView();
                }
                else {
                    showEmployeeView();
                }
            }
        });
 */