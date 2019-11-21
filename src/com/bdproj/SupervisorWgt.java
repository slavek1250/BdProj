package com.bdproj;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SupervisorWgt {
    private JPanel panelMain;
    private JTabbedPane tabbedPane1;
    private JTextField textField1;
    private JTextField textField2;
    private JButton dodajButton;
    private JComboBox comboBox1;
    private JTextField textField3;
    private JTextField textField4;
    private JButton zapiszZmianyButton;
    private JButton usuńPracownikaButton;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JCheckBox wUżytkuCheckBox;
    private JComboBox comboBox2;
    private JTextField textField8;
    private JCheckBox wUżytkuCheckBox1;
    private JButton usuńWyciągButton;
    private JButton zapiszZmianyButton1;
    private JTable table1;
    private JButton zapiszJakoNowyCennikButton;
    private JComboBox comboBox3;
    private JTextField textField9;
    private JTextField textField10;
    private JButton drukujRaportButton;
    private JButton btnLogout;

    private MainView mainView;

    public SupervisorWgt(MainView mainView) {
        this.mainView = mainView;

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mainView.showMainView(panelMain);
            }
        });
    }

    public JPanel getPanel() {
        return panelMain;
    }
}
