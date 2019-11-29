package com.bdproj;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SupervisorWgt extends Supervisor {
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
    private JComboBox boxLiftRepSelect;
    private JTextField txtLiftRepSince;
    private JTextField txtLiftRepTo;
    private JButton btnLogout;
    private JTextField txtPointsCostNewLift;
    private JTextField txtTicketUseRepNo;
    private JButton btnPrintLiftRep;
    private JLabel lblHello;
    private JButton btnTicketUseRep;

    private MainView mainView;

    public SupervisorWgt(MainView mainView, SystemUser user) {
        super(user);
        this.mainView = mainView;

        lblHello.setText("Witaj, " + systemUser.getName() + "!");

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mainView.showMainView();
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
