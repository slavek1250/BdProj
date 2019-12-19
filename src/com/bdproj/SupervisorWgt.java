package com.bdproj;

import javafx.event.Event;
import javafx.util.Pair;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class SupervisorWgt extends Supervisor {
    private JPanel panelMain;
    private JTabbedPane tabbedPane;
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
    private JComboBox boxLiftRepSelect;
    private JTextField txtLiftRepSince;
    private JTextField txtLiftRepTo;
    private JButton btnLogout;
    private JTextField txtPointsCostNewLift;
    private JTextField txtTicketUseRepNo;
    private JButton btnPrintLiftRep;
    private JLabel lblHello;
    private JButton btnPrintTicketUseRep;
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
    private JButton btnEmpNewPass;
    private JLabel lblUpTime;

    private MainView mainView;

    private final String NAME_REG_EX = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]{1,49}$";
    private final String SURNAME_REG_EX = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}(([a-ząćęłńóśźż]+)(-[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]+)?)$";
    private final int SURNAME_MAX_LENGTH = 50;
    private final String DATE_FORMAT = "yyyy-MM-dd";

    private String onlyNumbersRegEx = "^(?!(0))[0-9]{0,}$";
    private Uptime uptime;

    // TODO: Pracownicy: ladowanie pracownikow podleglych pod kierownika, walidacja danych wejsciowych. #Karol# !!DONE!!
    // TODO: Pracownicy: Mianowanie na kierownika, powinno automatycznie usuwać z listy pracowników pod kierownikiem ( w bazie ustaiwnie flagi jako pracownik zwolniony i kopia danych do kierownika ) #Karol#
    // TODO: Pracownicy: Przekazywanie kierownictwa. #Karol#
    // TODO: Wyciagi: ladowanie wyciagow podlegajacych pod kierownika (o ile obecna data jest w zakresie `od`, `do`, najlepiej `do` niech bedzie null) kosztów punktowych i stanu, walidacja danych wejsciowych (czy różne od bieżączych w przypadku edycji).
    // TODO: Wyciagi: ladowanie listy kieronikow, dodawanie jako zarzadce. Usuwanie swojego prawa do administorwania wyciągiem (o ile nie jest ostatnim kierownikiem mogącym zarządzać).
    // TODO: Wyciagi: Sprawdzenie czy wybrany kierownik nie jest w bieżącej grupie zarządców wyciągu(przy dodawaniu nowego zarządcy).
    // TODO: Cennik: Ladowanie biezacego cennika dla wszystkich pozycji ze slownika, walidacja danych wejsciowych. #Dominik# !!DONE!!
    // TODO: Raporty: Wybieranie dat dla raportu uzyc wyciagu, walidacja danych dla raportu uzycia biletu. #Dominik# !!DONE!!
    // TODO: Moje dane: Ladownianie obecnych danych kierownika, walidacja zmodyfikowanych. #Dominik# !!DONE!!


    public SupervisorWgt(MainView mainView, SystemUser user) {
        super(user);
        this.mainView = mainView;

        lblHello.setText("Witaj, " + systemUser.getName() + "!");

        if (!skiLiftAdmin.fetchSkiLifts()) {
            JOptionPane.showMessageDialog(panelMain, getLastError());
        }
        if (!fetchSupervisors()) {
            JOptionPane.showMessageDialog(panelMain, getLastError());
        }
        if (!fetchEmployees()) {
            JOptionPane.showMessageDialog(panelMain, getLastError());
        }

        btnLogout.addActionListener(actionEvent -> mainView.showMainView());
        btnAddNewEmpl.addActionListener(actionEvent -> addUser());
        boxSelectEditEmpl.addActionListener(actionEvent -> chooseUser());
        btnSaveEditEmpl.addActionListener(ActionEvent -> saveEmployeeMod());
        btnDeleteEmpl.addActionListener(ActionEvent -> deleteEmployee());
        btnPrintLiftRep.addActionListener(ActionEvent -> generateSkiLiftReport());
        btnPrintTicketUseRep.addActionListener(ActionEvent -> generateTicketReport());
        btnSaveSupervisor.addActionListener(ActionEvent -> updateSupervisorData());
        btnQuitJobSupervisor.addActionListener(ActionEvent -> quitJobSupervisor());
        btnNewAddLift.addActionListener(ActionEvent -> addLift());
        btnMakeSupervisorEmpl.addActionListener(ActionEvent -> promoteToSupervisor());
        btnChangeSupervisorEmpl.addActionListener(ActionEvent -> changeEmployeeSupervisor());
        btnMakeAdminLift.addActionListener(actionEvent -> promoteNewLiftOwner());
        btnDelAdminPrivLift.addActionListener(actionEvent -> quitManagingLift());
        btnSaveEdtiLift.addActionListener(actionEvent -> saveLiftMod());
        btnDeleteLift.addActionListener(actionEvent -> deleteSkiLift());
        btnEmpNewPass.addActionListener(actionEvent -> newEmpPassword());
        boxSelectEditLift.addActionListener(actionEvent -> chooseLift());
        tabbedPane.add((new PriceListWgt(systemUser)).panelMain, "Cennik", 3);
        uptime = new Uptime();
        uptime.setLabelToUpdate(lblUpTime);

        loadEmployees();
        loadReports();
        loadSupervisors();
        loadSupervisorData();


    }

    public JPanel getPanel() {
        return panelMain;
    }

    private void loadReports() {
        ArrayList<String> skiLifts = new ArrayList<>();
        skiLiftAdmin.skiLiftsList.stream().map(lift -> (lift.get(SkiLiftAdmin.SkiLiftsListEnum.ID) + ". " + lift.get(SkiLiftAdmin.SkiLiftsListEnum.NAME))).forEach(skiLifts::add);
        boxLiftRepSelect.setModel(new DefaultComboBoxModel(skiLifts.toArray()));
        boxSelectEditLift.setModel(new DefaultComboBoxModel(skiLifts.toArray()));

        boxLiftRepSelect.setSelectedIndex(-1);
        dateLiftRepSince.setFormats(DATE_FORMAT);
        dateLiftRepTo.setFormats(DATE_FORMAT);
        boxSelectEditLift.setSelectedIndex(-1);
    }

    private void loadSupervisors() {
        ArrayList<String> supLists = new ArrayList<>();
        supervisorsList.stream().map(sup -> (sup.get(SupervisorsListEnum.ID) + ". " + sup.get(SupervisorsListEnum.NAME) + " " + sup.get(SupervisorsListEnum.SURNAME))).forEach(supLists::add);
        boxSupervisorSelectLift.setModel(new DefaultComboBoxModel(supLists.toArray()));
        boxSupervisorSelectEmpl.setModel(new DefaultComboBoxModel(supLists.toArray()));
        boxSupervisorSelectLift.setSelectedIndex(-1);
        boxSupervisorSelectEmpl.setSelectedIndex(-1);
    }

    private void loadEmployees() {
        ArrayList<String> employees = new ArrayList<>();
        employeeList.stream().map(sup -> (sup.getKey() + ". " + sup.getValue().getValue() + " " + sup.getValue().getKey())).forEach(employees::add);
        boxSelectEditEmpl.setModel(new DefaultComboBoxModel(employees.toArray()));
        boxSelectEditEmpl.setSelectedIndex(-1);
    }

    private void generateSkiLiftReport() {
        if (boxLiftRepSelect.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(panelMain, "Nie wybrano żadnego wyciągu.");
            return;
        }
        String selectedSkiLift = boxLiftRepSelect.getSelectedItem().toString(); // Id. nazwa
        Integer skiLiftId = Integer.parseInt(selectedSkiLift.replaceAll("\\..*", ""));
        Date reportSince = dateLiftRepSince.getDate();
        Date reportTo = dateLiftRepTo.getDate();
        if (!reportSince.before(reportTo)) {
            JOptionPane.showMessageDialog(panelMain, "Początek okresu raportu musi być przed końcem tego okresu.");
            return;
        }
        if ((new Date()).before(reportTo)) {
            JOptionPane.showMessageDialog(panelMain, "Koniec okresu nie może być późniejszy niż " + (new SimpleDateFormat(DATE_FORMAT)).format(new Date()) + ".");
            return;
        }

        SkiLiftUseReport skiLiftUseReport = new SkiLiftUseReport(systemUser);
        boolean success = skiLiftUseReport.generateReport(skiLiftId, skiLiftAdmin.getSkiLiftName(skiLiftId), reportSince, reportTo);
        if (success) {
            saveReportAs(skiLiftUseReport);
        } else {
            JOptionPane.showMessageDialog(panelMain, skiLiftUseReport.getLastError());
        }
    }

    public void generateTicketReport() {
        String ticketNo = txtTicketUseRepNo.getText();
        if (!ticketNo.matches(onlyNumbersRegEx) || ticketNo.isEmpty()) {
            JOptionPane.showMessageDialog(panelMain, "Błędny format numeru, popraw i spróbuj ponownie.");
            return;
        }
        Integer ticketId = Integer.parseInt(ticketNo);

        TicketUseReport ticketUseReport = new TicketUseReport(systemUser);
        boolean success = ticketUseReport.generateReport(ticketId);
        if (success) {
            saveReportAs(ticketUseReport);
        } else {
            JOptionPane.showMessageDialog(panelMain, ticketUseReport.getLastError());
        }
    }

    public void saveReportAs(HtmlReport htmlReport) {
        boolean success = false;
        boolean tryToSave = true;
        Reports reports = new Reports(htmlReport);

        while (tryToSave) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Zapisz raport jako");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Plik PDF", "pdf"));

            int returnValue = fileChooser.showSaveDialog(panelMain);
            if (returnValue == JFileChooser.APPROVE_OPTION) {

                success = reports.saveReportToFile(fileChooser.getSelectedFile().getAbsolutePath());
                tryToSave = false;

            } else {
                int resp = JOptionPane.showConfirmDialog(panelMain, "Błąd podczas próby zapisu raportu, spróbować ponownie?", "Błąd", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.NO_OPTION) return;
            }
        }

        if (success) {
            JOptionPane.showMessageDialog(panelMain, "Pomyślnie zapisano raport.");
        } else {
            JOptionPane.showMessageDialog(panelMain, reports.getLastError());
        }
    }

    private void loadSupervisorData() {
        txtNameSupervisor.setText(systemUser.getName());
        txtSurnameSupervisor.setText(systemUser.getSurname());
    }

    private void updateSupervisorData() {
        String newName = txtNameSupervisor.getText();
        String newSurname = txtSurnameSupervisor.getText();

        if (systemUser.getName().equals(newName) && systemUser.getSurname().equals(newSurname)) {
            JOptionPane.showMessageDialog(panelMain, "Dane nie uległy zmianie.");
            return;
        }
        if (!newName.matches(NAME_REG_EX)) {
            JOptionPane.showMessageDialog(panelMain, "Podane imie jest w niepoprawnym formacie.");
            return;
        }
        if (!newSurname.matches(SURNAME_REG_EX) || newSurname.length() >= SURNAME_MAX_LENGTH) {
            JOptionPane.showMessageDialog(panelMain, "Podane nazwisko jest w niepoprawnym formacie.");
            return;
        }

        int resp = JOptionPane.showConfirmDialog(panelMain, "Czy na pewno zaktualizować dane?", "Potwierdź", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.NO_OPTION) {
            return;
        }

        systemUser.updateName(newName);
        systemUser.updateSurname(newSurname);
        if (systemUser.commitChanges()) {
            JOptionPane.showMessageDialog(panelMain, "Pomyślnie zapisano zmiany.");
        } else {
            JOptionPane.showMessageDialog(panelMain, systemUser.getLastError());
        }
    }

    private void quitJobSupervisor() {
        int resp = JOptionPane.showConfirmDialog(panelMain, "Czy na pewno chcesz zwolnić się z pracy?", "Potwierdź", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.NO_OPTION) {
            return;
        }

        if (systemUser.quitJob()) {
            JOptionPane.showMessageDialog(panelMain, "Zostałeś pomyślnie zwolniony z pracy.");
            mainView.showMainView();
        } else {
            JOptionPane.showMessageDialog(panelMain, systemUser.getLastError());
        }
    }

private void addUser(){

        String name = txtNameNewEmpl.getText();
        String surname = txtSurnameNewEmpl.getText();
        if (!name.matches(NAME_REG_EX) || !surname.matches(SURNAME_REG_EX) || surname.length() >= SURNAME_MAX_LENGTH) {
            JOptionPane.showMessageDialog(null, "Imie lub nazwisko zawiera niepoprawne znaki");
            return;
        } else {
            int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz dodać nowego pracownika?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
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
                fetchEmployees();
                loadEmployees();
            } else {
                return;
            }
        }
    }
//ąćęłńóśźż
    private String newLogin(String name, String surname) {
        Random rand = new Random();
        String randNumber = String.format("%04d", rand.nextInt(10000));
        String name1 = name.toLowerCase().replace("ą","a").replace("ę","e").replace("ć","c").replace("ł","l").
                replace("ń","n").replace("ó","o").replace("ś","s").replace("ź","z").replace("ż","z");
        String surname1 = surname.toLowerCase().replace("ą","a").replace("ę","e").replace("ć","c").replace("ł","l").
                replace("ń","n").replace("ó","o").replace("ś","s").replace("ź","z").replace("ż","z");;

        String login = (name1.substring(0, 3) + surname1.substring(0, 3) + randNumber);
        return login;
    }

    private void chooseUser() {
        if (boxSelectEditEmpl.getSelectedIndex() == -1) {
            return;
        }
        int id = getEmployeeId();
        txtNameEditEmpl.setText(getEmployeeName(id));
        txtSurnameEditEmpl.setText(getEmployeeSurname(id));
    }

    private Integer getEmployeeId() {
        String selectedEmp = boxSelectEditEmpl.getSelectedItem().toString(); // Id. nazwisko imie
        Integer employeeId = Integer.parseInt(selectedEmp.replaceAll("\\..*", ""));
        return employeeId;
    }

    private Integer getSupervisorId() {
        String selectedEmp = boxSupervisorSelectEmpl.getSelectedItem().toString(); // Id. nazwisko imie
        Integer supervisorId = Integer.parseInt(selectedEmp.replaceAll("\\..*", ""));
        return supervisorId;
    }
    private Integer getLiftSupervisorId() {
        String selectedEmp = boxSupervisorSelectLift.getSelectedItem().toString(); // Id. nazwisko imie
        Integer supervisorId = Integer.parseInt(selectedEmp.replaceAll("\\..*", ""));
        return supervisorId;
    }
    private Integer getLiftId() {
        String selectedEmp = boxSelectEditLift.getSelectedItem().toString(); // Id. nazwisko imie
        Integer supervisorId = Integer.parseInt(selectedEmp.replaceAll("\\..*", ""));
        return supervisorId;
    }

    private void saveEmployeeMod() {
        if (boxSelectEditEmpl.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego pracownika z listy.");
            return;
        }
        String name = txtNameEditEmpl.getText();
        String surname = txtSurnameEditEmpl.getText();
        int id = getEmployeeId();
        String givenName = getEmployeeName(id);
        String givenSurname = getEmployeeSurname(id);
        String loginRegEx = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]{1,50}$";
        if (!name.matches(loginRegEx) || !surname.matches(loginRegEx)) {
            JOptionPane.showMessageDialog(null, "Imie lub nazwisko zawiera niepoprawne znaki");
            return;
        } else {
            if (name.equals(givenName) && surname.equals(givenSurname)) {
                JOptionPane.showMessageDialog(null, "Dane użytkowinika się nie zmieniły");
            } else {

                int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zmodyfikować dane pracownika?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    employeeAdmin.saveModChanges(id, name, surname);
                    fetchEmployees();
                    loadEmployees();
                    txtNameEditEmpl.setText(null);
                    txtSurnameEditEmpl.setText(null);
                } else {
                    return;
                }
            }

        }
    }

    private void deleteEmployee() {
        if (boxSelectEditEmpl.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego pracownika z listy.");
            return;
        }
        int id = getEmployeeId();
        employeeAdmin.deleteEmployee(id);
        txtNameEditEmpl.setText(null);
        txtSurnameEditEmpl.setText(null);
        fetchEmployees();
        loadEmployees();
    }

    private void promoteToSupervisor() {
        if (boxSelectEditEmpl.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego pracownika z listy.");
            return;
        }
        int id = getEmployeeId();
        int supId=systemUser.getId();
        JPasswordField pass = new JPasswordField(8);


        int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz awansować " + getEmployeeName(id) + " " + getEmployeeSurname(id) + " na kierownika?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("Potwierdź hasłem:");
            panel.add(label);
            panel.add(pass);
            String[] pot = new String[]{"Potwierdź", "Anuluj"};
            int opcja = JOptionPane.showOptionDialog(null, panel, "Potwierdzenie", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, pot, pot[1]);
            if (opcja == 0) {
                String password= String.valueOf(pass.getPassword());

                if (!employeeAdmin.checkSamePassword(supId, password)) {
                    JOptionPane.showMessageDialog(null, "Hasło kierownika się nie zgadza");
                    return;
                }
                employeeAdmin.promoteToSupervisor(id);
                JOptionPane.showMessageDialog(null, getEmployeeName(id) + " " + getEmployeeSurname(id) + " jest teraz kierownikiem.");
                fetchEmployees();
                fetchSupervisors();
                loadEmployees();
                loadSupervisors();
                txtSurnameEditEmpl.setText(null);
                txtNameEditEmpl.setText(null);
            } else {
                return;
            }
        }
    }

    private void changeEmployeeSupervisor() {
        if (boxSelectEditEmpl.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego pracownika z listy.");
            return;
        }
        if (boxSupervisorSelectEmpl.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego kierownika z listy.");
            return;
        }
        int empId = getEmployeeId();
        int supId = getSupervisorId();
        int response = JOptionPane.showConfirmDialog(null,
                "Czy na pewno chcesz przypisać " + getEmployeeName(empId) + " " + getEmployeeSurname(empId) + " do: \n" + getSupervisorName(supId) + " " + getSupervisorSurname(supId) + "?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            employeeAdmin.changeEmployeeSupervisor(empId, supId);
            JOptionPane.showMessageDialog(null, "Nowym kierownikiem " + getEmployeeName(empId) + " " + getEmployeeSurname(empId) + " jest: " + getSupervisorName(supId) + " " + getSupervisorSurname(supId));
            fetchEmployees();
            loadEmployees();
            txtSurnameEditEmpl.setText(null);
            txtNameEditEmpl.setText(null);
        } else {
            return;
        }
    }

    private void newEmpPassword() {
        if (boxSelectEditEmpl.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego pracownika z listy.");
            return;
        }
        int id=getEmployeeId();
        PassGen passwd = new PassGen();
        String pass = passwd.generatePassword();
        int response = JOptionPane.showConfirmDialog(null,"Czy na pewno chcesz zmienić hasło dla "+getEmployeeName(id)+" "+getEmployeeSurname(id)+"?","Potwierdzenie",JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            employeeAdmin.changeEmployeePass(id,pass);
            JOptionPane.showMessageDialog(null,"Nowe hasło pracownika "+getEmployeeName(id)+" "+getEmployeeSurname(id)+" to: \n"+pass);
        }
        else{return;}
    }

    private void addLift(){
        String name = txtNameNewLift.getText();
        String height = txtHeightNewLift.getText();
        String pointsCost = txtPointsCostNewLift.getText();
        int idSup = systemUser.getId();
        boolean state = checkStateNewLift.isSelected();
        if(!height.matches(onlyNumbersRegEx) || !pointsCost.matches(onlyNumbersRegEx)||height.matches("") ||pointsCost.matches("") ){
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

    private void chooseLift() {
        if (boxSelectEditLift.getSelectedIndex() == -1) {
            return;
        }
       int id=getLiftId();
        boolean state;
        if(skiLiftAdmin.getSkiLiftState(id).matches("1")){
            state=true;}else state=false;
        txtPointsCostEditLift.setText(skiLiftAdmin.getSkiLiftPoints(id));
        chechStateEditLift.setSelected(state);
    }

    private void promoteNewLiftOwner() {
        if (boxSelectEditLift.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego wyciągu z listy.");
            return;
        }
        if (boxSupervisorSelectLift.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego kierownika z listy.");
            return;
        }
        int supId=getLiftSupervisorId();
        int liftId=getLiftId();
        JPasswordField pass = new JPasswordField(8);
        int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz przekazać wyciąg: " + skiLiftAdmin.getSkiLiftName(liftId) + " do kierownika \n"+getSupervisorName(supId)+" "+getSupervisorSurname(supId)+"?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("Potwierdź hasłem:");
            panel.add(label);
            panel.add(pass);
            String[] pot = new String[]{"Potwierdź", "Anuluj"};
            int opcja = JOptionPane.showOptionDialog(null, panel, "Potwierdzenie", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, pot, pot[1]);
            if (opcja == 0) {
                String password = String.valueOf(pass.getPassword());

                if (!employeeAdmin.checkSamePassword(supId, password)) {
                    JOptionPane.showMessageDialog(null, "Hasło kierownika się nie zgadza");
                    return;
                }
                if(skiLiftAdmin.promoteNewLiftSupervisor(supId,liftId)){
                    JOptionPane.showMessageDialog(null,getSupervisorName(supId)+" "+getSupervisorSurname(supId)+" jest zarządcą wyciągu "+ skiLiftAdmin.getSkiLiftName(liftId));
                    boxSelectEditLift.setSelectedIndex(-1);
                    boxSupervisorSelectLift.setSelectedIndex(-1);

                }

            }
        }
    }
    private void quitManagingLift(){
        if (boxSelectEditLift.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego wyciągu z listy.");
            return;
        }
        int liftId=getLiftId();
        int response = JOptionPane.showConfirmDialog(null,"Czy na pewno chcesz usunąć swoje prawo do zarządzania wyciągiem "+skiLiftAdmin.getSkiLiftName(liftId)+"?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            if(skiLiftAdmin.quitManagingLift(liftId)){
             JOptionPane.showMessageDialog(null,"Nie jesteś już zarzadcą wyciągu "+skiLiftAdmin.getSkiLiftName(liftId)+".");
             skiLiftAdmin.fetchSkiLifts();
             loadReports();
            }else{return;}
        }
    }

    private void saveLiftMod(){
        if (boxSelectEditLift.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego wyciągu z listy.");
            return;
        }
        int liftId=getLiftId();
        String point=txtPointsCostEditLift.getText();
        boolean givenState=chechStateEditLift.isSelected();
        boolean state;
        if(skiLiftAdmin.getSkiLiftState(liftId).matches("1")){state=true;}else{state=false;}

        if(point.matches(skiLiftAdmin.getSkiLiftPoints(liftId))&&(Boolean.compare(state,givenState))==0){
            JOptionPane.showMessageDialog(null,"Nie wprowadzono żadnych zmian");
            return;

        }
        if(!point.matches(onlyNumbersRegEx)){
            JOptionPane.showMessageDialog(null,"Niedozwolone dane wejściowe. Liczba punktów powinna być liczbą!");
            return;
        }
        boolean pointsBool;
        boolean stateBool;
        String state2="Włączony",state1="Włączony";
        if(!point.matches(skiLiftAdmin.getSkiLiftPoints(liftId))){pointsBool=true;}else{pointsBool=false;}
        if(Boolean.compare(state,givenState)==1){
            stateBool=true;
            if(state==true) { state1="Włączony"; }else{state1="Wyłaczony";}
            if (givenState==true){state2="Włączony";}else{state2="Wyłączony";}
        }else{stateBool=false;}
         int response = JOptionPane.showConfirmDialog(null,"Czy na pewno chcesz zmodyfikować dane wyciągu: "+skiLiftAdmin.getSkiLiftName(liftId)+"?" +
                (pointsBool==false ? "" :"\nZmiana punktów z : "+skiLiftAdmin.getSkiLiftPoints(liftId)+" na "+point+".")+
                (stateBool==false ? "" :"\nZmiana stanu z: "+state1+" na "+state2+"."), "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            skiLiftAdmin.saveSkiLiftChanges(point, state, liftId);
            JOptionPane.showMessageDialog(null,"Dane wyciągu zostały zmodyfikowane pomyślnie.");
            skiLiftAdmin.fetchSkiLifts();
            loadReports();
            boxSupervisorSelectLift.setSelectedIndex(-1);
            txtPointsCostEditLift.setText(null);
            chechStateEditLift.setSelected(false);
        }
    }

    private void deleteSkiLift(){
        if (boxSelectEditLift.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego wyciągu z listy.");
            return;
        }
        int liftId=getLiftId();
        int response = JOptionPane.showConfirmDialog(null,"Czy na pewno chcesz usunąć wyciąg "+skiLiftAdmin.getSkiLiftName(liftId)+"?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
        skiLiftAdmin.deleteSkiLift(liftId);
        JOptionPane.showMessageDialog(null,"Wyciąg "+skiLiftAdmin.getSkiLiftName(liftId)+" został pomyślnie usunięty.");
        skiLiftAdmin.fetchSkiLifts();
        loadReports();
        txtPointsCostEditLift.setText(null);
        chechStateEditLift.setSelected(false);

        }

    }
}



