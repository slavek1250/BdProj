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
            logincheckwho();
            }
        });
    }

    public boolean isSupervisor() {
        return txtLogin.getText().equals("kier");
    }

    public void showMainView(JPanel toHide) {
        frame.remove(toHide);
        frame.setContentPane(panelMain);
        frame.setSize(500,200);
        panelMain.updateUI();
    }

    public void showSupervisorView() {
        frame.remove(panelMain);
        frame.setContentPane(supervisorWgt.getPanel());
        frame.setSize(700,500);
        supervisorWgt.getPanel().updateUI();
    }

    public void showEmployeeView() {
        frame.remove(panelMain);
        frame.setContentPane(employeeWgt.getPanel());
        frame.setSize(600,500);
        employeeWgt.getPanel().updateUI();
    }

    public void logincheckwho(){
        PreparedStatement ps;
        ResultSet rs;
        String username= txtLogin.getText();
        String password= String.valueOf(passwLogin.getPassword());
        String query=   "SELECT 'K' as WHO FROM kierownik WHERE login like ? AND haslo like MD5(?)\n" +
                "union all\n" +
                "SELECT 'P' as WHO FROM pracownicy WHERE login like ? AND haslo like MD5(?)";
        try {
            ps = MySQLConnection.getConnection().prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, username);
            ps.setString(4, password);
            rs = ps.executeQuery();

            if (rs.first()) {
                String who = rs.getString("WHO");
                if (who.equals("K")) {
                    showSupervisorView();
                    return;
                } else if (who.equals("P")) {
                    showEmployeeView();
                    return;
                }
            }

            JOptionPane.showMessageDialog(null, "COS POSZLO NIE TAK ZIOMEK");

        } catch(SQLException ex){
            Logger.getLogger(MainWin.class.getName()).log(Level.SEVERE,null, ex);
        }
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