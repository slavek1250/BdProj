package com.bdproj;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWin implements MainView {

    static JFrame frame;
    static SupervisorWgt supervisorWgt;
    static EmployeeWgt employeeWgt;
    private JButton btnLogin;
    private JPanel panelMain;
    private JTextField txtLogin;
    private JPasswordField passwordField1;

    public MainWin() {
        btnLogin.addActionListener(new ActionListener() {
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