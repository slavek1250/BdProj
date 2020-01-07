package com.bdproj.sys_admin;

import com.bdproj.sys_admin.PriceList;
import com.bdproj.sys_admin.PriceListPrint;
import com.bdproj.sys_admin.Reports;
import com.bdproj.sys_admin.SystemUser;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.Vector;

/**
 * Klasa odpowiedzialna za GUI cennika.
 * @see PriceList
 */
public class PriceListWgt extends PriceList {
    private JTable tabPriceList;            /**< Tabela cennika. */
    private JButton btnSaveNewPriceList;    /**< Przycisk zapisu nowego / modyfikacji cennika. */
    private JLabel lblPriceListAuthor;      /**< Etykieta autora. */
    private JLabel lblPriceListSince;       /**< Etykieta daty początku obowiązywania cennika. */
    private JComboBox boxSelectPriceList;   /**< ComboBox do wyboru cennika. */
    private JButton btnDeletePriceList;     /**< Przycisk usuwania cannika. */
    public JPanel panelMain;                /**< Panel widoku. */
    private JXDatePicker dateValidSince;    /**< Wybieranie daty wdrożenia cennika. */
    private JButton btnPrintPriceList;      /**< Przycisk drukowania cennika. */

    /**
     * Wyrażenie regularne walidujące format ceny.
     */
    private final String PRICE_VALIDATOR = "[0-9]+(.[0-9]{1,2})?";
    /**
     * Indeks kolumny w tabeli zawieracjącej ceny.
     */
    private final int PRICE_COLUMN = 2;

    /**
     * Domyślny konstruktor.
     * @param user Obiekt użytkownika systemu.
     */
    PriceListWgt(SystemUser user) {
        super(user);
        loadHeaders();

        btnSaveNewPriceList.addActionListener(actionEvent -> savePriceList());
        btnDeletePriceList.addActionListener(actionEvent -> deletePriceList());
        btnPrintPriceList.addActionListener(actionEvent -> printPriceList());
        boxSelectPriceList.addActionListener(actionEvent -> priceListSelectionHasChanged());

        boxSelectPriceList.setSelectedIndex(boxSelectPriceList.getModel().getSize() > 0 ? 0 : -1);
        dateValidSince.setFormats(DATE_FORMAT);
    };

    /**
     * Metoda odświeżająca dane lokalne, danymi z bazy.
     * @see loadHeaders()
     */
    public void refresh() {
        String currentPriceList = boxSelectPriceList.getSelectedItem().toString();
        loadHeaders();
        boxSelectPriceList.setSelectedItem(currentPriceList);
        loadPriceList();
    }

    /**
     * Metoda ładująca do ComboBox nagłówki cenników.
     * @see PriceList::fetchPriceListsHeaders()
     */
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
        if(!isDataBaseIsEmpty()) {
            boxSelectPriceList.setModel(new DefaultComboBoxModel(names.toArray()));
        }
        else {
            loadPriceList();
        }
    }

    /**
     * Callback zmiany obecnie wybranego cennika. Pobiera szczegóły nowo wybranego cennika.
     */
    private void priceListSelectionHasChanged() {
        if(boxSelectPriceList.getSelectedIndex() != -1) {
            String selectedPriceListName = boxSelectPriceList.getSelectedItem().toString();
            if (boxSelectPriceList.getSelectedIndex() == -1 || selectedPriceListName.equals(super.getCurrentName())) {
                return;
            }
            super.setSelectedPriceList(selectedPriceListName);

            loadPriceList();
        }
    }

    /**
     * Metoda uzupełniająca pola GUI pobranym lokalnie cennikiem.
     * @see PriceList::fetchSinglePriceList()
     */
    private void loadPriceList() {

        if(!super.fetchSinglePriceList()) {
            JOptionPane.showMessageDialog(panelMain, super.getLastError());
            return;
        }

        boolean canEdit = super.hasModifyPrivileges();

        DefaultTableModel  tableModel = new DefaultTableModel() {
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
            dateValidSince.setEditable(!isDataBaseIsEmpty());
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

    /**
     * Metoda zapisująca cennik. Sprawdza czy:
     *  - wprowadzona została jakakolwiek zmiana,
     *  - wybrana data nie posiada przypisanego już cennika w bazie.
     * Jeżeli jest to aktualny cenniki to zapisuje jako nowy, jeżeli jeszcze nie wszedł w życie zapisuje zmiany.
     * @see PriceList::saveAsNewPriceList()
     * @see PriceList::updateCurrentPriceList()
     */
    private void savePriceList() {

        boolean anyPriceHasChanged = false;
        Date validSince = dateValidSince.getDate();
        ArrayList<EnumMap<PriceListEnum, String>> newPriceListItems = new ArrayList<>();
        for(int i = 0; i < super.selectedPriceList.size(); i++) {

            String cellPrice = (String)tabPriceList.getValueAt(i, PRICE_COLUMN);

            if(!cellPrice.matches(PRICE_VALIDATOR)) {
                JOptionPane.showMessageDialog(panelMain, "Ta wartość: " + cellPrice + " nie jest ceną w poprawnym formacie.");
                return;
            }

            if(!super.selectedPriceList.get(i).get(PriceListEnum.PRICE).equals(cellPrice)) {
                anyPriceHasChanged = true;
            }
            EnumMap<PriceListEnum, String> newItem = new EnumMap<>(PriceListEnum.class);
            newItem.put(PriceListEnum.PRICE_LIST_DICTIONARY_ID, super.selectedPriceList.get(i).get(PriceListEnum.PRICE_LIST_DICTIONARY_ID));
            newItem.put(PriceListEnum.NAME, super.selectedPriceList.get(i).get(PriceListEnum.NAME));
            newItem.put(PriceListEnum.PRICE, cellPrice);
            newPriceListItems.add(newItem);
        }

        try {
            anyPriceHasChanged |= isDataBaseIsEmpty() || (validSince.compareTo((new SimpleDateFormat(DATE_FORMAT)).parse(super.getValidSince())) != 0) & !super.isPresentPriceList();
        }
        catch (ParseException ex) {
            JOptionPane.showMessageDialog(panelMain, ex.getMessage());
            return;
        }
        if(!anyPriceHasChanged) {
            JOptionPane.showMessageDialog(panelMain, "Nie wprowadzono żadnych zmian.");
            return;
        }
        if(!isDataBaseIsEmpty() && !validSince.after(new Date())) {
            JOptionPane.showMessageDialog(panelMain, "Data wprowadzenia musi być poźniejsza niż obecna.");
            return;
        }
        if(!super.checkIfDateIsUnique(validSince)) {
            JOptionPane.showMessageDialog(panelMain, super.getLastError());
            return;
        }

        int resp = JOptionPane.showConfirmDialog(panelMain, "Czy potwierdzasz chęć wykonania tej operacji?", "Potwierdź", JOptionPane.YES_NO_OPTION);
        boolean reloadPriceListTable;

        if(resp == JOptionPane.NO_OPTION) {
            resp = JOptionPane.showConfirmDialog(panelMain, "Czy załadować niezmodyfikowny cennik?", "Zadecyduj", JOptionPane.YES_NO_OPTION);
            reloadPriceListTable = resp == JOptionPane.YES_OPTION;
        }
        else {
            boolean success = super.isPresentPriceList() ? saveAsNewPriceList(validSince, newPriceListItems) : updateCurrentPriceList(validSince, newPriceListItems);

            if (success) {
                JOptionPane.showMessageDialog(panelMain, "Zakończono operację opwodzeniem.");
            } else {
                JOptionPane.showMessageDialog(panelMain, super.getLastError());
            }
            reloadPriceListTable = success;
        }

        loadHeaders();
        boxSelectPriceList.setSelectedItem(super.getCurrentName());
        if(reloadPriceListTable) {
            loadPriceList();
        }
    }

    /**
     * Metoda usuwająca cennik bieżący cennik. Operacja możliwa tylko i wyłączenie jeżeli cennik jeszcze niewszedł w życie.
     * @see PriceList::deleteCurrentPriceList()
     */
    private void deletePriceList() {
        int resp = JOptionPane.showConfirmDialog(panelMain, "Czy na pewno chcesz usunąć ten cennik?\nUwaga! Operacja nieodwracalna!", "Potwierdź", JOptionPane.YES_NO_OPTION);
        if(resp == JOptionPane.NO_OPTION) {
            return;
        }

        if(super.deleteCurrentPriceList()) {
            JOptionPane.showMessageDialog(panelMain, "Pomyślnie usunięto cennik.");
            loadHeaders();
            boxSelectPriceList.setSelectedItem(boxSelectPriceList.getModel().getElementAt(0).toString());
        }
        else {
            JOptionPane.showMessageDialog(panelMain, super.getLastError());
        }
    }

    /**
     * Metoda odpowiedzialna za wydruk cennika do pliku pdf.
     * @see PriceListPrint
     * @see Reports
     */
    private void printPriceList() {
        if(isDataBaseIsEmpty()) return;

        String currentPriceList = boxSelectPriceList.getSelectedItem().toString();
        loadHeaders();
        boxSelectPriceList.setSelectedItem(currentPriceList);

        String validSince = super.getValidSince();
        String validTo = super.getValidTo();
        ArrayList<EnumMap<PriceListEnum, String>> priceListItems = super.selectedPriceList;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Zapisz cennik jako");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Plik PDF", "pdf"));

        int returnValue = fileChooser.showSaveDialog(panelMain);
        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }

        PriceListPrint priceListPrint = new PriceListPrint(validSince, validTo, priceListItems);
        if(!priceListPrint.generatePriceListHtml()) {
            JOptionPane.showMessageDialog(panelMain, priceListPrint.getLastError());
            return;
        }

        Reports reports = new Reports(priceListPrint);
        if(reports.saveReportToFile(fileChooser.getSelectedFile().getAbsolutePath())) {
            JOptionPane.showMessageDialog(panelMain, "Pomyślnie zapisano cennik.");
        }
        else {
            JOptionPane.showMessageDialog(panelMain,reports.getLastError());
        }
    }
}

