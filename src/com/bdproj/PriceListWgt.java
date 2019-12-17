package com.bdproj;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.Vector;

public class PriceListWgt extends PriceList {
    private JTable tabPriceList;
    private JButton btnSaveNewPriceList;
    private JLabel lblPriceListAuthor;
    private JLabel lblPriceListSince;
    private JComboBox boxSelectPriceList;
    private JButton btnDeletePriceList;
    public JPanel panelMain;
    private JXDatePicker dateValidSince;

    private final String DATE_FORMAT = "yyyy-MM-dd";
    private final String PRICE_VALIDATOR = "[0-9]+(.[0-9]{1,2})?";
    private final int PRICE_COLUMN = 2;


    PriceListWgt(SystemUser user) {
        super(user);
        loadHeaders();

        btnSaveNewPriceList.addActionListener(actionEvent -> savePriceList());
        boxSelectPriceList.addActionListener(actionEvent -> loadPriceList());

        boxSelectPriceList.setSelectedIndex(boxSelectPriceList.getModel().getSize() > 0 ? 0 : -1);
        dateValidSince.setFormats(DATE_FORMAT);
    };

    private void loadHeaders() {
        if(!super.fetchPriceListsHeaders()) {
            JOptionPane.showMessageDialog(panelMain, super.getLastError());
            return;
        }
        ArrayList<String> names = new ArrayList<>();
        super.priceListsHeadersList
                .stream()
                .map(item -> item.get(PriceListsHeadersEnum.NAME))
                .forEach(names::add);
        boxSelectPriceList.setModel(new DefaultComboBoxModel(names.toArray()));
    }

    private void loadPriceList() {

        String selectedPriceListName = boxSelectPriceList.getSelectedItem().toString();
        if(boxSelectPriceList.getSelectedIndex() == -1 || selectedPriceListName.equals(super.getCurrentName())) {
            return;
        }

        super.setSelectedPriceList(selectedPriceListName);
        if(!super.fetchSinglePriceList()) {
            JOptionPane.showMessageDialog(panelMain, super.getLastError());
            return;
        }

        boolean canEdit = super.hasModifyPrivileges();

        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return (canEdit && column == PRICE_COLUMN);
            }
        };

        tableModel.addColumn("#");
        tableModel.addColumn("Nazwa");
        tableModel.addColumn("Cena");
        tableModel.addColumn("Jednostka");

        for(EnumMap<PriceListEnum, String> item : super.selectedPriceList) {
            Vector<String> row = new Vector<>();
            row.add(item.get(PriceListEnum.PRICE_LIST_DICTIONARY_ID));
            row.add(item.get(PriceListEnum.NAME));
            row.add(item.get(PriceListEnum.PRICE));
            row.add(super.getUnit());
            tableModel.addRow(row);
        }

        tabPriceList.setModel(tableModel);

        lblPriceListAuthor.setText("Autor cennika: " + super.getAuthor());
        lblPriceListSince.setText("Ważny od: " + super.getValidSince());


        if(super.isPresentPriceList()) {
            btnSaveNewPriceList.setText("Zapisz jako nowy cennik");
            dateValidSince.setDate(new Date());
            dateValidSince.setEditable(true);
            btnSaveNewPriceList.setEnabled(true);
            btnDeletePriceList.setEnabled(false);
        }
        else {
            btnSaveNewPriceList.setText("Zapisz zmiany");
            try {
            dateValidSince.setDate(new SimpleDateFormat(DATE_FORMAT).parse(super.getValidSince()));
            }
            catch (ParseException ex) {
                JOptionPane.showMessageDialog(panelMain, ex.getMessage());
            }
            dateValidSince.setEditable(canEdit);
            btnSaveNewPriceList.setEnabled(canEdit);
            btnDeletePriceList.setEnabled(canEdit);
        }
    }

    private void savePriceList() {

        boolean anyPriceHasChanged = false;
        Date validSince = dateValidSince.getDate();
        ArrayList<String> newPrices = new ArrayList<>();
        for(int i = 0; i < super.selectedPriceList.size(); i++) {

            String cellPrice = (String)tabPriceList.getValueAt(i, PRICE_COLUMN);

            if(!cellPrice.matches(PRICE_VALIDATOR)) {
                JOptionPane.showMessageDialog(panelMain, "Ta wartość: " + cellPrice + " nie jest ceną w poprawnym formacie.");
                return;
            }

            if(!super.selectedPriceList.get(i).get(PriceListEnum.PRICE).equals(cellPrice)) {
                anyPriceHasChanged = true;
            }
            newPrices.add(cellPrice);
        }
        if(!validSince.after(new Date())) {
            JOptionPane.showMessageDialog(panelMain, "Data wprowadzenia musi być poźniejsza niż obecna.");
            return;
        }
        if(!super.checkIfIsUniqueDate()) {
            JOptionPane.showMessageDialog(panelMain, "Podana data istnieje już w systemie, spróbuj wybrać inną.");
            return;
        }

        /*
        boolean anyPriceHasChanged = false;
        String priceValidator = "[0-9]+(.[0-9]{1,2})?";
        ArrayList<Double> priceListPrices = super.getPriceListPrices();
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

        int resp = JOptionPane.showConfirmDialog(panelMain, "Czy na pewno chcesz dodać nowy cennik?\nJeżeli zrezygnujesz zostanie załadowny poprzeni cennik.", "Potwierdź", JOptionPane.YES_NO_OPTION);
        if(resp == JOptionPane.NO_OPTION) {
            //loadPriceList();
            return;
        }

        if(anyPriceHasChanged) {
            super.setPriceListPrices(newPriceListPrices);
            if(super.createNewPriceList()) {
                loadPriceListDetails();
                JOptionPane.showMessageDialog(panelMain, "Pomyślnie dodano nowy cennik.");
            }
            else {
                JOptionPane.showMessageDialog(panelMain, super.getLastError());
            }
        }
        else {
            JOptionPane.showMessageDialog(panelMain, "Nie wprowadzono żadnych zmian w cenniku.");
        }

         */
    }

    private void loadPriceListDetails() {
        lblPriceListAuthor.setText("Autor cennika: " + super.getAuthor());
        lblPriceListSince.setText("Ważny od: " + super.getValidSince());
    }
}
