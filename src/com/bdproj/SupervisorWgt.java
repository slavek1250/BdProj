package com.bdproj;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SupervisorWgt {
    private JPanel panelMain;
    private JTabbedPane tabbedPane1;
    private JTextField txtNameNewEmpl;
    private JTextField txtSurnameNewEmpl;
    private JButton btnAddNewEmpl;
    private JComboBox boxSelectEditEmpl;
    private JTextField txtNameEditEmpl;
    private JTextField txtSurnameEditEmpl;
    private JButton btnSaveEditEmpl;
    private JButton btnDeleteEmpl;
    private JTextField txtNameNewLift;
    private JTextField txtHeightNewLift;
    private JCheckBox checkStateNewLift;
    private JComboBox boxSelectEditLift;
    private JTextField txtPointsCostEditLift;
    private JCheckBox chechStateEditLift;
    private JButton btnDeleteLift;
    private JButton btnSaveEdtiLift;
    private JTable tabPriceList;
    private JButton saveNewPriceList;
    private JComboBox comboBox3;
    private JTextField textField9;
    private JTextField textField10;
    private JButton btnLogout;
    private JTextField txtPointsCostNewLift;
    private JTextField textField1;
    private JButton btnPrintLiftRep;

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

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
