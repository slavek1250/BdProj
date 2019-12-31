package com.bdproj;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private JCheckBox checkStateEditLift;
    private JButton btnDeleteLift;
    private JButton btnSaveEditLift;
    private JComboBox boxLiftRepSelect;
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

    /**
     * Domyślny konstruktor.
     * @param mainView Obiekt głównego widoku aplikacji.
     * @param user Obiekt zalogowanego użytkownika systemu.
     */
    public SupervisorWgt(MainView mainView, SystemUser user) {
        super(user);
        this.mainView = mainView;

        lblHello.setText("Witaj, " + systemUser.getName() + "!");

        if (!skiLiftAdmin.fetchSkiLifts()) {
            JOptionPane.showMessageDialog(panelMain, skiLiftAdmin.getLastError());
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
        btnSaveEditLift.addActionListener(actionEvent -> saveLiftMod());
        btnDeleteLift.addActionListener(actionEvent -> deleteSkiLift());
        btnEmpNewPass.addActionListener(actionEvent -> newEmpPassword());
        boxSelectEditLift.addActionListener(actionEvent -> chooseLift());
        tabbedPane.add((new PriceListWgt(systemUser)).panelMain, "Cennik", 3);
        uptime = new Uptime();
        uptime.setLabelToUpdate(lblUpTime);

        loadEmployees();
        loadSkiLifts();
        loadSupervisors();
        loadSupervisorData();

        dateLiftRepSince.setFormats(DATE_FORMAT);
        dateLiftRepTo.setFormats(DATE_FORMAT);
    }

    /**
     * Getter.
     * @return Zwraca panel widoku.
     */
    public JPanel getPanel() {
        return panelMain;
    }

    /**
     * Metoda ładująca listę wyciągów, których zarządcą jest obecnie zalogowany kierownik, do pól ComboBox.
     * @see SkiLiftAdmin::fetchSkiLifts()
     */
    private void loadSkiLifts() {
        ArrayList<String> skiLifts = new ArrayList<>();
        skiLiftAdmin.skiLiftsList.stream().map(lift -> (lift.get(SkiLiftAdmin.SkiLiftsListEnum.ID) + ". " + lift.get(SkiLiftAdmin.SkiLiftsListEnum.NAME))).forEach(skiLifts::add);
        boxLiftRepSelect.setModel(new DefaultComboBoxModel(skiLifts.toArray()));
        boxSelectEditLift.setModel(new DefaultComboBoxModel(skiLifts.toArray()));

        boxLiftRepSelect.setSelectedIndex(-1);
        boxSelectEditLift.setSelectedIndex(-1);
    }

    /**
     * Metoda ładująca listę wszystkich niezwolnionych kierowników do pól ComboBox.
     * @see Supervisor::fetchSupervisors()
     */
    private void loadSupervisors() {
        ArrayList<String> supLists = new ArrayList<>();
        supervisorsList.stream().map(sup -> (sup.get(SupervisorsListEnum.ID) + ". " + sup.get(SupervisorsListEnum.NAME) + " " + sup.get(SupervisorsListEnum.SURNAME))).forEach(supLists::add);
        boxSupervisorSelectLift.setModel(new DefaultComboBoxModel(supLists.toArray()));
        boxSupervisorSelectEmpl.setModel(new DefaultComboBoxModel(supLists.toArray()));
        boxSupervisorSelectLift.setSelectedIndex(-1);
        boxSupervisorSelectEmpl.setSelectedIndex(-1);
    }

    /**
     * Metoda ładująca listę niezwolnionych pracowników, których kierownikiem jest obecnie zalogowany kierownik, do pól ComboBox.
     * @see Supervisor::fetchEmployees()
     */
    private void loadEmployees() {
        ArrayList<String> employees = new ArrayList<>();
        employeeList.stream().map(sup -> (sup.get(EmployeeListEnum.ID) + ". " + sup.get(EmployeeListEnum.NAME) + " " + sup.get(EmployeeListEnum.SURNAME))).forEach(employees::add);
        boxSelectEditEmpl.setModel(new DefaultComboBoxModel(employees.toArray()));
        boxSelectEditEmpl.setSelectedIndex(-1);
    }

    /**
     * Metoda wypełniająca pola edycji danych kierownika, danymi obecnie zalogowanego kierownika.
     * @see SystemUser::getName()
     * @see SystemUser::getSurname()
     */
    private void loadSupervisorData() {
        txtNameSupervisor.setText(systemUser.getName());
        txtSurnameSupervisor.setText(systemUser.getSurname());
    }

    /**
     * Metoda odpowiedzialna za generację raportu użyć wyciągu. Sprawdza:
     *  - czy podano prawidłowy zakres dat,
     *  - czy wybrano jakikolwiek wyciąg.
     * @see SkiLiftUseReport
     */
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

    /**
     * Metoda odpowiedzialna za generację repotu użyć biletu.
     * Sprawdza czy podano numer id istniejącego biletu.
     * @see TicketUseReport
     */
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

    /**
     * Metoda odpowiedzialna za zapis raportów do pliku.
     * Pyta użytkownika gdzie zapisać plik.
     * @param htmlReport Raport sformatowany zgodnie z szablonem HTML.
     * @see Reports
     */
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

    /**
     * Metoda odpowiedzialna za aktualizację danych kierownika. Sprawdza:
     *  - poprawność danych,
     *  - czy dana uległy zmianie.
     * @see SystemUser::updateName()
     * @see SystemUser::updateSurname()
     * @see SystemUser::commitChanges()
     */
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

    /**
     * Metoda odpowiedzialna za zolnienie się z pracy przez kierownika.
     * @see SystemUser::quitJob()
     */
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
        return getIdFromComboBox(boxSelectEditEmpl);
    }
    private Integer getSupervisorId() {
        return getIdFromComboBox(boxSupervisorSelectEmpl);
    }
    private Integer getLiftSupervisorId() {
        return getIdFromComboBox(boxSupervisorSelectLift);
    }
    private Integer getLiftId() {
        return getIdFromComboBox(boxSelectEditLift);
    }
    private Integer getIdFromComboBox(JComboBox comboBox) {
        if(comboBox.getSelectedIndex() != -1) {
            String selectedEmp = comboBox.getSelectedItem().toString(); // Id. nazwisko imie
            return Integer.parseInt(selectedEmp.replaceAll("\\..*", ""));
        }
        return -1;
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
        if (!name.matches(NAME_REG_EX) || !surname.matches(SURNAME_REG_EX)) {
            JOptionPane.showMessageDialog(null, "Imie lub nazwisko zawiera niepoprawne znaki");
            return;
        } else {
            if (name.equals(givenName) && surname.equals(givenSurname)) {
                JOptionPane.showMessageDialog(null, "Dane pracownika " +getEmployeeName(id)+" "+getEmployeeSurname(id)+ " się nie zmieniły.");
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
        int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zwolnić pracownika "+getEmployeeName(id)+" "+getEmployeeSurname(id)+"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            employeeAdmin.deleteEmployee(id);
            txtNameEditEmpl.setText(null);
            txtSurnameEditEmpl.setText(null);
            fetchEmployees();
            loadEmployees();
        }else{return;}
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
        if(!name.matches("")) {JOptionPane.showMessageDialog(null,"Nazwa wyciągu nie może być pusta!");return;}
        if(!height.matches(onlyNumbersRegEx) || !pointsCost.matches(onlyNumbersRegEx)||height.matches("") ||pointsCost.matches("") ){
            JOptionPane.showMessageDialog(null,"Niedozwolone dane wejściowe. Wysokość i koszt powinny być liczbą!");
        }
        else {
            int response= JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz dodać wyciąg?","Confirm",JOptionPane.YES_NO_OPTION);
            if(response==JOptionPane.YES_OPTION) {
                skiLiftAdmin.addNewLift(name, height, pointsCost, state, idSup);
                JOptionPane.showMessageDialog(null, "Dodano wyciąg");
                if(!skiLiftAdmin.fetchSkiLifts()) {
                    JOptionPane.showMessageDialog(panelMain, skiLiftAdmin.getLastError());
                    return;
                }
                loadSkiLifts();
                skiLiftAdmin.fetchSkiLifts();
                loadSkiLifts();
            }
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
        checkStateEditLift.setSelected(state);
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

                if (!employeeAdmin.checkSamePassword(systemUser.getId(), password)) {
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
             loadSkiLifts();
            }else{return;}
        }
    }

    private void saveLiftMod() {
        if (boxSelectEditLift.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano żadnego wyciągu z listy.");
            return;
        }
        int liftId = getLiftId();
        String point = txtPointsCostEditLift.getText();
        boolean givenState = checkStateEditLift.isSelected();
        boolean state;
        if (skiLiftAdmin.getSkiLiftState(liftId).matches("1")) {
            state = true;
        } else {
            state = false;
        }

        if (point.matches(skiLiftAdmin.getSkiLiftPoints(liftId)) && (Boolean.compare(state, givenState)) == 0) {
            JOptionPane.showMessageDialog(null, "Nie wprowadzono żadnych zmian");
            return;

        }
        if (!point.matches(onlyNumbersRegEx)) {
            JOptionPane.showMessageDialog(null, "Niedozwolone dane wejściowe. Liczba punktów powinna być liczbą!");
            return;
        }
        boolean pointsBool;
        boolean stateBool;
        String state2 = "Włączony", state1 = "Włączony";
        if (!point.matches(skiLiftAdmin.getSkiLiftPoints(liftId))) {
            pointsBool = true;
        } else {
            pointsBool = false;
        }
        if (!(Boolean.compare(state, givenState) == 0)) {
            pointsBool = !point.matches(skiLiftAdmin.getSkiLiftPoints(liftId));
            if (Boolean.compare(state, givenState) == 1) {
                stateBool = true;
                state1 = state ? "Włączony" : "Wyłaczony";
                state2 = givenState ? "Włączony" : "Wyłączony";
            } else {
                stateBool = false;
            }
            int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zmodyfikować dane wyciągu: " + skiLiftAdmin.getSkiLiftName(liftId) + "?" +
                    (pointsBool == false ? "" : "\nZmiana punktów z : " + skiLiftAdmin.getSkiLiftPoints(liftId) + " na " + point + ".") +
                    (stateBool == false ? "" : "\nZmiana stanu z: " + state1 + " na " + state2 + "."), "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                String setState;
                if (givenState == true) {
                    setState = "1";
                } else {
                    setState = "0";
                }
                skiLiftAdmin.saveSkiLiftChanges(point, setState, liftId);
                JOptionPane.showMessageDialog(null, "Dane wyciągu zostały zmodyfikowane pomyślnie.");
                skiLiftAdmin.fetchSkiLifts();
                loadSkiLifts();
                txtPointsCostEditLift.setText(null);
                checkStateEditLift.setSelected(false);
            }
        }
    }

        private void deleteSkiLift () {
            if (boxSelectEditLift.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(null, "Nie wybrano żadnego wyciągu z listy.");
                return;
            }
            int liftId = getLiftId();
            int response = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz usunąć wyciąg " + skiLiftAdmin.getSkiLiftName(liftId) + "?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                skiLiftAdmin.deleteSkiLift(liftId);
                JOptionPane.showMessageDialog(null, "Wyciąg " + skiLiftAdmin.getSkiLiftName(liftId) + " został pomyślnie usunięty.");
                skiLiftAdmin.fetchSkiLifts();
                loadSkiLifts();
                txtPointsCostEditLift.setText(null);
                checkStateEditLift.setSelected(false);
            }
        }
    }