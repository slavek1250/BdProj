package com.bdproj;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Klasa GUI przeznaczona dla kierowników.
 */

public class SupervisorWgt extends Supervisor {
    private JPanel panelMain; /** <Panel główny.*/
    private JTabbedPane tabbedPane; /** <Obsługa zakładek.*/
    private JTextField txtNameNewEmpl;/** <Pole tekstowe do wpisania imienia nowego pracownika.*/
    private JTextField txtSurnameNewEmpl; /** <Pole tekstowe do wpisania nazwiska nowego pracownika.*/
    private JButton btnAddNewEmpl; /** <Przycisk dodawania nowego użytkownika.*/
    private JComboBox boxSelectEditEmpl; /** <Combobox do wybierania pracowników.*/
    private JTextField txtNameEditEmpl; /** <Pole tekstowe wyświetlające imie wybranego z listy pracownika.*/
    private JTextField txtSurnameEditEmpl; /** <Pole tekstowe wyświetlające nazwisko wybranego z listy pracownika.*/
    private JButton btnSaveEditEmpl; /** <Przycisk do zapisywania modyfikacji danych pracownika.*/
    private JButton btnDeleteEmpl; /** <Przycisk do usuwania pracownika.*/
    private JTextField txtNameNewLift; /** <Pole tekstowe do wpisania nazwy nowego wyciągu.*/
    private JTextField txtHeightNewLift; /** <Pole tekstowe do wpisania wysokości nowego wyciągu.*/
    private JCheckBox checkStateNewLift; /** <Checkbox do określenia stanu nowego wyciągu (włączony/wyłączony).*/
    private JComboBox boxSelectEditLift; /** <Combobox do wybierania wyciągu.*/
    private JTextField txtPointsCostEditLift; /** <Pole tekstowe wyświetlające liczbę punktów wybranego wyciągu.*/
    private JCheckBox checkStateEditLift; /** <Checkbox wyświetlający stan wybranego wyciągu.*/
    private JButton btnDeleteLift; /** <Przycisk do usuwania wyciągu.*/
    private JButton btnSaveEditLift; /** <Przycisk do zapisywania modyfikacji danych wyciągu.*/
    private JComboBox boxLiftRepSelect; /** <Combobox do wybierania wyciągu.*/
    private JButton btnLogout; /** <Przycisk do wylogowania.*/
    private JTextField txtPointsCostNewLift; /** <Pole tekstowe do wpisania ilości punktów nowego wyciągu.*/
    private JTextField txtTicketUseRepNo; /** <Pole tekstowe do wpisania numeru biletu dla którego ma się wygenerować raport.*/
    private JButton btnPrintLiftRep; /** <Przycisk do drukowania raportu o wyciągu.*/
    private JLabel lblHello; /** <Pole wyświetlające imie aktulanie zalogowanego użytkownika.*/
    private JButton btnPrintTicketUseRep; /** <Przycisk do drukowania raportu o użyciu danego biletu.*/
    private JButton btnDelAdminPrivLift; /** <Przycisk do usuwania prawa kierownika nad danym wyciągiem.*/
    private JButton btnMakeSupervisorEmpl; /** <Przycisk do promowania wybranego pracownika na kierownika.*/
    private JTextField txtSurnameSupervisor; /** <Pole tekstowe wyświetlające imie aktualnie zalogowanego kierownika.*/
    private JTextField txtNameSupervisor; /** <Pole tekstowe wyświetlające nazwisko aktualnie zalogowanego kierownika.*/
    private JButton btnSaveSupervisor; /** <Przycisk do zapisywania modyfikacji danych aktualnie zalogowanego kierownika.*/
    private JButton btnQuitJobSupervisor; /** <Przycisk do usuwania aktualnie zalogowanego kierownika.*/
    private JComboBox boxSupervisorSelectLift; /** <Combobox do wybierania wyciągu. */
    private JButton btnMakeAdminLift; /** <Przycisk do mianowania wybranego kierownika na zarządzce wyciągu.*/
    private JComboBox boxSupervisorSelectEmpl; /** <Combobox do wybierania z kierownika z listy.*/
    private JButton btnChangeSupervisorEmpl; /** <Przycisk do zmiany wybranego pracownika do innego kierownika.*/
    private JButton btnNewAddLift; /** <Przycisk do dodwania nowego wyciągu.*/
    private JXDatePicker dateLiftRepSince; /** <Pole służące do wyboru daty od.*/
    private JXDatePicker dateLiftRepTo; /** <Pole służące do wyboru daty do.*/
    private JButton btnEmpNewPass; /** <Przycisk do generownia nowego hasła dla pracownika.*/
    private JLabel lblUpTime; /** <Pole wyświetlające czas jaki użytkownik jest zalogowany w systemie.*/

    private MainView mainView; /**<Główny panel GUI. */

    private final String NAME_REG_EX = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]{1,49}$"; /**<Zmienna pomocnicza do sprawdzania poprawności wpisanego imienia.*/
    private final String SURNAME_REG_EX = "^[A-ZĄĆĘŁŃÓŚŹŻ]{1}(([a-ząćęłńóśźż]+)(-[A-ZĄĆĘŁŃÓŚŹŻ]{1}[a-ząćęłńóśźż]+)?)$"; /**<Zmienna pomocnicza do sprawdzania poprawności wpisanego nazwiska.*/
    private final int SURNAME_MAX_LENGTH = 50; /**<Zmienna pomocnicza do sprawdzania maksymalnej długości nazwiska.*/
    private final String DATE_FORMAT = "yyyy-MM-dd"; /**<Zmienna pomocnicza do ustalania formatu daty.*/
    private String onlyNumbersRegEx = "^(?!(0))[0-9]{0,}$"; /**<Zmienna pomocnicza do sprawdzania poprawności wpisanej liczby.*/
    private Uptime uptime; /**<Obiekt do liczenia czasu zalogowania w systemie.*/

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

    /**
     * Metoda dodająca nowego pracownika do systemu. Sprawdzana jest poprawność wpisanych danych, generowany jest login oraz hasło dla pracownika. Sprawdzane jest również czy taki login już nie istnieje w systemie.
     * @see PassGen
     * @see newlogin()
     * @see EmployeeAdmin::checkSameLogin()
     * @see EmployeeAdmin::addNewUser()
     */
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

    /**
     *
     * @param name Imie nowego pracownika.
     * @param surname Nazwisko nowego pracownika.
     * @return Zwraca login w postaci iiinnncccc, gdzie: i-pierwsze trzy litery imienia, n- pierwsze trzy litery nazwiska, c- cyfry losowo wygenerowane od 0-9.
     */
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

    /**
     * Metoda do wybierania pracownika z Comboboxa a następnie umieszczenie imienia i nazwiska w polach tekstowych.
     * @see getEmployeeId()
     */
    private void chooseUser() {
        if (boxSelectEditEmpl.getSelectedIndex() == -1) {
            return;
        }
        int id = getEmployeeId();
        txtNameEditEmpl.setText(getEmployeeName(id));
        txtSurnameEditEmpl.setText(getEmployeeSurname(id));
    }

    /**
     * Metoda do pobierania id wybranego pracownika z Combobox.
     * @return Zwraca id wybranego pracownika.
     * @see getIdFromComboBox()
     */
    private Integer getEmployeeId() {
        return getIdFromComboBox(boxSelectEditEmpl);
    }
    /**
     * Metoda do pobierania id wybranego kierownika z Combobox.
     * @return Zwraca id wybranego kierownika.
     */
    private Integer getSupervisorId() {
        return getIdFromComboBox(boxSupervisorSelectEmpl);
    }
    /**
     * Metoda do pobierania id wybranego kierownika z Combobox.
     * @return Zwraca id wybranego kierownika.
     * @see getIdFromComboBox()
     */
    private Integer getLiftSupervisorId() {
        return getIdFromComboBox(boxSupervisorSelectLift);
    }
    /**
     * Metoda do pobierania id wybranego wyciągu z Combobox.
     * @return Zwraca id wybranego wyciągu.
     * @see getIdFromComboBox()
     */
    private Integer getLiftId() {
        return getIdFromComboBox(boxSelectEditLift);
    }

    /**
     * Metoda która pobiera do zmiennej cała linie wybranego tekstu z Comboboxa, a następnie usuwa wszystko co znajduje się za liczbą.
     * @param comboBox Obiekt Combobox z którego chcemy wyciągnąć indeks.
     * @return Zwraca indeks z wybranego Comboboxa.
     */
    private Integer getIdFromComboBox(JComboBox comboBox) {
        if(comboBox.getSelectedIndex() != -1) {
            String selectedEmp = comboBox.getSelectedItem().toString(); // Id. nazwisko imie
            return Integer.parseInt(selectedEmp.replaceAll("\\..*", ""));
        }
        return -1;
    }

    /**
     * Metoda służąca do zapisywania modyfikacji danych pracownika. Sprawdzane jest czy dane się zmieniły oraz czy nowe dane pracownika są w poprawnym formacie.
     * @see EmployeeAdmin::saveModChanges()
     */
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

    /**
     * Metoda służąca do usuwania pracownika.
     * @see EmployeeAdmin::deleteEmployee()
     */
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

    /**
     * Metoda służąca do zmiany awansowania pracownika na kierownika. Aby dokonać awansu należy również wpisać hasło dla obecnie zalogowanego kierownika, które jest potwierdzeniem wykonania awansu.
     * @see EmployeeAdmin::checkSamePassword()
     * @see EmployeeAdmin::promoteToSupervisor()
     */
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

    /**
     * Metoda służąca do przypisania jednego z pracowników podległych pod obecnie zalogowanego kierownika, do innego kierownika w systemie.
     * @see EmployeeAdmin::changeEmployeeSupervisor()
     */
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

    /**
     * Metoda służąca do generacji nowego hasła dla pracownika.
     * @see EmployeeAdmin::changeEmployeePass()
     */
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

    /**
     * Metoda służąca do dodawania nowego wyciągu.
     * @see SystemUser::getId()
     * @see SkiLiftAdmin::addNewLift()
     */
    private void addLift(){
        String name = txtNameNewLift.getText();
        String height = txtHeightNewLift.getText();
        String pointsCost = txtPointsCostNewLift.getText();
        int idSup = systemUser.getId();
        boolean state = checkStateNewLift.isSelected();
        if(name.matches("")) {JOptionPane.showMessageDialog(null,"Nazwa wyciągu nie może być pusta!");return;}
        if(!height.matches(onlyNumbersRegEx) || !pointsCost.matches(onlyNumbersRegEx)||height.matches("") ||pointsCost.matches("") ){
            JOptionPane.showMessageDialog(null,"Niedozwolone dane wejściowe. Wysokość i koszt powinny być liczbą!");
        }
        else {
            int response= JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz dodać wyciąg "+name+" ?","Confirm",JOptionPane.YES_NO_OPTION);
            if(response==JOptionPane.YES_OPTION) {
                skiLiftAdmin.addNewLift(name, height, pointsCost, state, idSup);
                JOptionPane.showMessageDialog(null, "Pomyślnie dodano nowy wyciąg.");
                if(!skiLiftAdmin.fetchSkiLifts()) {
                    JOptionPane.showMessageDialog(panelMain, skiLiftAdmin.getLastError());
                    return;
                }
                loadSkiLifts();
                skiLiftAdmin.fetchSkiLifts();
                loadSkiLifts();
                txtNameNewLift.setText(null);
                txtHeightNewLift.setText(null);
                txtPointsCostNewLift.setText(null);
                checkStateNewLift.setSelected(false);
            }
        }
    }
    /**
     * Metoda do wybierania wyciągu z Comboboxa a następnie umieszczenie nazwy wyciągu w polu tekstowym oraz stanu wyciągu w CheckBox.
     * @see getLiftId()
     */
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

    /**
     * Metoda służąca do dodania nowego kierownika w systemie uprawnień do kierownia danym wyciągiem. Sprawdzane jest czy wybrany kierownik nie jest już zarządzca danego wyciągu. Potwierdzeniem tego działania jest wpisanie przez kierownika obecnie zalogowanego w systemie swojego hasła.
     * @see getLiftSupervisorId()
     * @see getLiftId()
     * @see EmployeeAdmin::checkSamePassword
     * @see SkiLiftAdmin::promoteNewLiftSupervisor()
     */
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

    /**
     * Metoda służąca do usunięcia swoich praw do zarządzania danym wyciągiem.
     * @see getLiftId()
     * @see SkiLiftAdmin::quitManagingLift()
     */
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

    /**
     * Metoda służąca do zapisywania modyfikacji danych danego wyciągu. Sprawdzane jest również czy dokonane zostały zmiany i czy wprowadzone dane są poprawne.
     * @see getLiftId()
     * @see SkiLiftAdmin::getSkiLiftState()
     * @see SkiLiftAdmin::getSkiLiftPoints()
     * @see SkiLiftAdmin::saveSkiLiftChanges()
     */
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

    /**
     * Metoda służąca do usuwania danego wyciągu.
     * @see getLiftId()
     * @see SkiLiftAdmin::deleteSkiLift()
     */
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