package com.bdproj;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

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

    // TODO: Pracownicy: ladowanie pracownikow podleglych pod kierownika, walidacja danych wejsciowych.
    // TODO: Wyciagi: ladowania wyciagow podlegajacych pod kierownika, walidacja danych wejsciowych.
    // TODO: Raporty: Wybieranie dat dla raportu uzyc wyciagu, walidacja danych dla raportu uzycia biletu.

    public SupervisorWgt(MainView mainView, SystemUser user) {
        super(user);
        this.mainView = mainView;

        lblHello.setText("Witaj, " + systemUser.getName() + "!");

        loadPriceList();


        btnLogout.addActionListener(actionEvent -> mainView.showMainView());
        saveNewPriceList.addActionListener(actionEvent -> savePriceList());
    }

    public JPanel getPanel() {
        return panelMain;
    }

    // ??
    /*
    private void createUIComponents() {

    }
    */

    private void loadPriceList() {
        if(priceList.fetchPriceList()) {
            ArrayList<String> priceListNames = priceList.getPriceListNames();
            ArrayList<Double> priceListPrices = priceList.getPriceListPrices();

            DefaultTableModel tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0;
                }
            };
            tableModel.addColumn("Nazwa");
            tableModel.addColumn("Cena");

            for (int i = 0; i < priceListNames.size(); i++) {
                Vector<String> row = new Vector<String>(2);
                row.add(priceListNames.get(i));
                row.add(priceListPrices.get(i) != -1 ? priceListPrices.get(i).toString() : "");
                tableModel.addRow(row);
            }
            tabPriceList.setModel(tableModel);
        }
    }

    private void savePriceList() {
        boolean anyPriceHasChanged = false;
        String priceValidator = "[0-9]+(.[0-9]{1,2})?";
        ArrayList<Double> priceListPrices = priceList.getPriceListPrices();
        ArrayList<Double> newPriceListPrices = new ArrayList<Double>();

        for(int i = 0; i < priceListPrices.size(); i++) {

            String cellVal = (String) tabPriceList.getValueAt(i, 1); // ??

            if(!cellVal.matches(priceValidator)) {
                JOptionPane.showMessageDialog(panelMain, "Ta wartość: " + cellVal + " nie jest ceną w poprawnym formacie.");
                return;
            }
            Double cellPrice = Double.parseDouble(cellVal);
            if(!priceListPrices.get(i).equals(cellPrice)) {
                anyPriceHasChanged = true;
            }
            newPriceListPrices.add(cellPrice);
        }

        if(anyPriceHasChanged) {
            priceList.setPriceListPrices(newPriceListPrices);
            if(priceList.createNewPriceList()) {
                JOptionPane.showMessageDialog(panelMain, "Pomyślnie dodano nowy cennik.");
            }
            else {
                JOptionPane.showMessageDialog(panelMain, priceList.getLastError());
            }
        }
        else {
            JOptionPane.showMessageDialog(panelMain, "Nie wprowadzono żadnych zmian w cenniku.");
        }
    }
}
