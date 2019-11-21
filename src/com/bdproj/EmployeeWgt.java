package com.bdproj;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EmployeeWgt {
    private JPanel panelMain;
    private JButton btnLogout;
    private JTextField textField1;
    private JCheckBox nowyBiletCheckBox;
    private JTextField textField2;
    private JComboBox comboBox1;
    private JButton wydrukujBiletButton;
    private JButton doładujBiletButton;
    private JTextField textField3;
    private JButton zablokujBiletButton;

    private MainView mainView;

    public EmployeeWgt(MainView mainView) {
        this.mainView = mainView;

        nowyBiletCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {


            }
        });
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
