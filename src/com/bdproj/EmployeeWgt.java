package com.bdproj;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EmployeeWgt {
    public JPanel panelMain;
    private JButton wylogujButton;
    private JTextField textField1;
    private JCheckBox nowyBiletCheckBox;
    private JTextField textField2;
    private JComboBox comboBox1;
    private JButton wydrukujBiletButton;
    private JButton doładujBiletButton;
    private JTextField textField3;
    private JButton zablokujBiletButton;

    public EmployeeWgt() {
        nowyBiletCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
            }
        });
    }
}
