package com.bdproj;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWin {
    private JButton button1;
    private JPanel panelMain;
    private JTextField textField1;
    private JPasswordField passwordField1;

    public MainWin() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(null, "Hello World!");
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("WYCIĄG NARCIARSKI U SKOCZKA");
        EmployeeWgt mainWin = new EmployeeWgt();


        mainWin.panelMain.setSize(500,500);
        frame.setContentPane(mainWin.panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
