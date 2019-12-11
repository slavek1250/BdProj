package com.bdproj;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;
import java.util.Random;


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
    private JLabel lblPriceListAuthor;
    private JLabel lblPriceListSince;
    private JButton btnDelAdminPrivLift;
    private JButton btnMakeSupervisorEmpl;
    private JTextField txtSurnameSupervisor;
    private JTextField txtNameSupervisor;
    private JButton btnSaveSupervisor;
    private JButton btnQuitJobSupervisor;
    private JComboBox boxSupervisorSelectLift;
    private JButton btnMakeAdminLift;
    private JComboBox boxSupervisorSelectEmpl;
    private JButton btnChangeSupervisorEmpl;
    private JButton btnNewAddLift;
    private JXDatePicker dateLiftRepSince;
    private JXDatePicker dateLiftRepTo;
    private JPanel tmpdate;

    private MainView mainView;

    private final String NAME_REG_EX = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]{1,49}$";
    private final String SURNAME_REG_EX = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}(([a-ząćęłńóśźż]+)(-[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]+)?)$";
    private final int SURNAME_MAX_LENGTH = 50;
    private final String DATE_FORMAT = "yyyy-MM-dd";

    private String nameRegEx = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]{1,49}$";
    private String surnameRegEx = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}(([a-ząćęłńóśźż]+)(-[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]+)?)$";
    private int surnameMaxLength = 50;
    private String onlyNumbersRegEx = "\\d+";

    // TODO: Pracownicy: ladowanie pracownikow podleglych pod kierownika, walidacja danych wejsciowych. #Karol# !!DONE!!
    // TODO: Pracownicy: Mianowanie na kierownika, powinno automatycznie usuwać z listy pracowników pod kierownikiem ( w bazie ustaiwnie flagi jako pracownik zwolniony i kopia danych do kierownika ) #Karol#
    // TODO: Pracownicy: Przekazywanie kierownictwa. #Karol#
    // TODO: Wyciagi: ladowanie wyciagow podlegajacych pod kierownika (o ile obecna data jest w zakresie `od`, `do`, najlepiej `do` niech bedzie null) kosztów punktowych i stanu, walidacja danych wejsciowych (czy różne od bieżączych w przypadku edycji).
    // TODO: Wyciagi: ladowanie listy kieronikow, dodawanie jako zarzadce. Usuwanie swojego prawa do administorwania wyciągiem (o ile nie jest ostatnim kierownikiem mogącym zarządzać).
    // TODO: Cennik: Ladowanie biezacego cennika dla wszystkich pozycji ze slownika, walidacja danych wejsciowych. #Dominik# !!DONE!!
    // TODO: Raporty: Wybieranie dat dla raportu uzyc wyciagu, walidacja danych dla raportu uzycia biletu. #Dominik#
    // TODO: Moje dane: Ladownianie obecnych danych kierownika, walidacja zmodyfikowanych. #Dominik# !!DONE!!


    public SupervisorWgt(MainView mainView, SystemUser user) {
        super(user);
        this.mainView = mainView;

        lblHello.setText("Witaj, " + systemUser.getName() + "!");

        if(!fetchSkiLifts()) {
            JOptionPane.showMessageDialog(panelMain, getLastError());
        }
        if(!fetchSupervisors()) {
            JOptionPane.showMessageDialog(panelMain, getLastError());
        }

        btnLogout.addActionListener(actionEvent -> mainView.showMainView());
        saveNewPriceList.addActionListener(actionEvent -> savePriceList());
        btnAddNewEmpl.addActionListener(actionEvent -> addUser());
        boxSelectEditEmpl.addActionListener(actionEvent ->chooseUser(actionEvent));
        btnSaveEditEmpl.addActionListener(ActionEvent ->saveEmployeeMod());
        btnDeleteEmpl.addActionListener(ActionEvent ->deleteEmployee());
        btnPrintLiftRep.addActionListener(ActionEvent -> generateSkiLiftReport());
        btnSaveSupervisor.addActionListener(ActionEvent -> updateSupervisorData());
        btnQuitJobSupervisor.addActionListener(ActionEvent -> quitJobSupervisor());
        btnNewAddLift.addActionListener(ActionEvent -> addLift());

        loadPriceList();
        loadEmployees();
        loadReports();
        loadSupervisorData();
    }

    public JPanel getPanel() {
        return panelMain;
    }

    private void loadReports() {
        ArrayList<String> skiLifts = new ArrayList<>();
        skiLiftsList.stream().map(lift -> (lift.getKey() + ". " + lift.getValue())).forEach(skiLifts::add);
        boxLiftRepSelect.setModel(new DefaultComboBoxModel(skiLifts.toArray()));

        boxLiftRepSelect.setSelectedIndex(-1);
        dateLiftRepSince.setFormats(DATE_FORMAT);
        dateLiftRepTo.setFormats(DATE_FORMAT);
    }

    private void generateSkiLiftReport() {
        if(boxLiftRepSelect.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(panelMain, "Nie wybrano żadnego wyciągu.");
            return;
        }
        String selectedSkiLift = boxLiftRepSelect.getSelectedItem().toString(); // Id. nazwa
        Integer skiLiftId = Integer.parseInt(selectedSkiLift.replaceAll("\\..*", ""));

        JOptionPane.showMessageDialog(null, getSkiLiftName(skiLiftId));
    }

    private void loadSupervisorData() {
        txtNameSupervisor.setText(systemUser.getName());
        txtSurnameSupervisor.setText(systemUser.getSurname());
    }

    private void updateSupervisorData() {
        String newName = txtNameSupervisor.getText();
        String newSurname = txtSurnameSupervisor.getText();

        if(systemUser.getName().equals(newName) && systemUser.getSurname().equals(newSurname)) {
            JOptionPane.showMessageDialog(panelMain, "Dane nie uległy zmianie.");
            return;
        }
        if(!newName.matches(NAME_REG_EX)) {
            JOptionPane.showMessageDialog(panelMain, "Podane imie jest w niepoprawnym formacie.");
            return;
        }
        if(!newSurname.matches(SURNAME_REG_EX) || newSurname.length() >= SURNAME_MAX_LENGTH) {
            JOptionPane.showMessageDialog(panelMain, "Podane nazwisko jest w niepoprawnym formacie.");
            return;
        }

        int resp = JOptionPane.showConfirmDialog(panelMain, "Czy na pewno zaktualizować dane?", "Potwierdź", JOptionPane.YES_NO_OPTION);
        if(resp == JOptionPane.NO_OPTION) {
            return;
        }

        systemUser.updateName(newName);
        systemUser.updateSurname(newSurname);
        if(systemUser.commitChanges()) {
            JOptionPane.showMessageDialog(panelMain, "Pomyślnie zapisano zmiany.");
        }
        else {
            JOptionPane.showMessageDialog(panelMain, systemUser.getLastError());
        }
    }

    private void quitJobSupervisor() {
        int resp = JOptionPane.showConfirmDialog(panelMain, "Czy na pewno chcesz zwolnić się z pracy?", "Potwierdź", JOptionPane.YES_NO_OPTION);
        if(resp == JOptionPane.NO_OPTION) {
            return;
        }

        if(systemUser.quitJob()) {
            JOptionPane.showMessageDialog(panelMain, "Zostałeś pomyślnie zwolniony z pracy.");
            mainView.showMainView();
        }
        else {
            JOptionPane.showMessageDialog(panelMain, systemUser.getLastError());
        }
    }

    private void loadPriceList() {
        Integer nameColumn = 0;
        Integer unitColumn = 2;

        if(priceList.fetchPriceList()) {
            loadPriceListDetails();

            ArrayList<String> priceListNames = priceList.getPriceListNames();
            ArrayList<Double> priceListPrices = priceList.getPriceListPrices();

            DefaultTableModel tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return (column != nameColumn && column != unitColumn);
                }
            };
            tableModel.addColumn("Nazwa");
            tableModel.addColumn("Cena");
            tableModel.addColumn("Jednostka");

            for (int i = 0; i < priceListNames.size(); i++) {
                Vector<String> row = new Vector<String>(2);
                row.add(priceListNames.get(i));
                row.add(priceListPrices.get(i) != -1 ? priceListPrices.get(i).toString() : "");
                row.add(priceList.getUnit());
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
                loadPriceListDetails();
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

    private void loadPriceListDetails() {
        lblPriceListAuthor.setText("Autor cennika: " + priceList.getAuthor());
        lblPriceListSince.setText("Ważny od: " + priceList.getValidSince());
    }

private void addUser(){

    String name= txtNameNewEmpl.getText();
    String surname= txtSurnameNewEmpl.getText();
    if(!name.matches(NAME_REG_EX)||!surname.matches(SURNAME_REG_EX) || surname.length() >= SURNAME_MAX_LENGTH){
        JOptionPane.showMessageDialog(null,"Imie lub nazwisko zawiera niepoprawne znaki");
        return;
    }
    else{
        int response= JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz dodać nowego pracownika?","Confirm",JOptionPane.YES_NO_OPTION);
        if(response==JOptionPane.YES_OPTION) {
            PassGen passwd = new PassGen();
            String password = passwd.generatePassword();
            String login = newLogin(name, surname);
            while (employeeAdmin.checkSameLogin(login)) {
                newLogin(name, surname);
            }
            employeeAdmin.addNewUser(name, surname, login, password);
            JOptionPane.showMessageDialog(null, "Login: " + login + "\n Hasło: " + password);
            txtNameNewEmpl.setText(null);
            txtSurnameNewEmpl.setText(null);
            loadEmployees();
        }
        else{return;}
    }
}

private String newLogin (String name, String surname){
    Random rand=new Random();
    String randNumber=String.format("%04d", rand.nextInt(10000));
    String name1=name.toLowerCase();
    String surname1=surname.toLowerCase();
    String login= (name1.substring(0,3)+surname1.substring(0,3)+randNumber);
    return login;
}

private void loadEmployees (){

       ArrayList employees= employeeAdmin.getEmployees();
       boxSelectEditEmpl.setModel(new DefaultComboBoxModel(employees.toArray()));
}

private void chooseUser(ActionEvent e){
        JComboBox comboBox=(JComboBox) e.getSource();
    String user= (String)boxSelectEditEmpl.getSelectedItem();
    employeeAdmin.splitSelected(user);
    txtNameEditEmpl.setText(employeeAdmin.getName());
    txtSurnameEditEmpl.setText(employeeAdmin.getSurname());
    }

    private void saveEmployeeMod(){
    String name=txtNameEditEmpl.getText();
    String surname=txtSurnameEditEmpl.getText();
    String givenName=employeeAdmin.getName();
    String givenSurname=employeeAdmin.getSurname();
    String loginRegEx ="^[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]{1,50}$";
    if(!name.matches(loginRegEx)||!surname.matches(loginRegEx)) {
        JOptionPane.showMessageDialog(null,"Imie lub nazwisko zawiera niepoprawne znaki");
        return;
    }
    else {
        if (name.equals(givenName) && surname.equals(givenSurname)) {
            JOptionPane.showMessageDialog(null,"Dane użytkowinika się nie zmieniły");
        }
        else{

            int response=JOptionPane.showConfirmDialog(null,"Czy na pewno chcesz znowdyfikować dane pracownika?","Confirm",JOptionPane.YES_NO_OPTION);
            if (response==JOptionPane.YES_OPTION) {
                employeeAdmin.saveModChanges(name, surname);
                loadEmployees();
            }
            else{return;}
        }

    }
    }

    private void deleteEmployee(){
        String name=txtNameEditEmpl.getText();
        String surname=txtSurnameEditEmpl.getText();
        employeeAdmin.deleteEmployee();
}

    private void addLift(){
        String name = txtNameNewLift.getText();
        String height = txtHeightNewLift.getText();
        String pointsCost = txtPointsCostNewLift.getText();
        int idSup = systemUser.getId();
        boolean state = checkStateNewLift.isSelected();
        //skiLiftAdmin.addNewLift(name, height, pointsCost, state, idSup);
        if(!height.matches(onlyNumbersRegEx) || !pointsCost.matches(onlyNumbersRegEx) ){
            JOptionPane.showMessageDialog(null,"Niedozwolone dane wejściowe. Wysokość i koszt powinny być liczbą!");
            return;
        }
        else{
            int response= JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz dodać wyciąg?","Confirm",JOptionPane.YES_NO_OPTION);
            if(response==JOptionPane.YES_OPTION) {
                skiLiftAdmin.addNewLift(name, height, pointsCost, state, idSup);
                JOptionPane.showMessageDialog(null, "Dodano wyciąg");
            }
            else{return;}
        }
    }

}



