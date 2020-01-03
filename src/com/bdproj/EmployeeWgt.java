package com.bdproj;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;

/**
 * Klasa GUI przeznaczona dla pracowników.
 */
public class EmployeeWgt extends Employee {
    private JPanel panelMain; /** <Panel główny.*/
    private JButton btnLogout; /** <Przycisk wylogowania.*/
    private JTextField txtTicketNo; /** <Pole tekstowe do wpisania numeru biletu.*/
    private JCheckBox checkNewTicket; /** <Checkbox określający czy jest to nowy bilet.*/
    private JTextField txtTicketPoints; /** <Pole tekstowe do wpisania liczby punktów.*/
    private JComboBox boxSelectPriceList; /** <Combobox do wybierania pozycji z cennika.*/
    private JButton btnPrintTicket; /** <Przycisk do dodania nowego biletu.*/
    private JButton btnTopUp; /** <Przycisk do doładowania biletu.*/
    private JTextField txtDeleteTicketNo; /** <Pole tekstowe do wpisania numeru biletu który ma być usunięty.*/
    private JButton btnDeleteTicket; /** <Przcisk do usuwania biletu.*/
    private JLabel lblHello; /** <Pole wyświetlające imie akutalnie zalogowanego pracownika.*/
    private JLabel lblUptime; /** <Pole wyświetlające czas jaki pracownik jest zalogowany w systemie.*/
    private Uptime uptime; /** <Obiekt do liczenia czasu zalogowania w systemie.*/

    private MainView mainView;
    private String onlyNumbersRegEx = "^(?!(0))[0-9]{0,}$"; /** <Zmienna pomocnicza do sprawdzania czy wpisane dane to tylko liczby od 0-9*/

    /**
     * Domyślny konstruktor.
     * @param mainView Obiekt głównego widoku aplikacji.
     * @param user Obiekt zalogowanego użytkownika systemu.
     */
    public EmployeeWgt(MainView mainView, SystemUser user) {
        super(user);
        this.mainView = mainView;

        lblHello.setText("Witaj, " + systemUser.getName() + "!");
        btnPrintTicket.setEnabled(false);

        if(!tickets.fetchCurrentPriceList()) {
            JOptionPane.showMessageDialog(panelMain, tickets.getLastError());
        }

        btnLogout.addActionListener(actionEvent -> mainView.showMainView());

        btnTopUp.addActionListener(actionEvent -> topUpTicket());
        btnPrintTicket.addActionListener(actionEvent -> createNewTicket());
        checkNewTicket.addItemListener(actionEvent -> newTicketSlot(actionEvent));
        btnDeleteTicket.addActionListener(actionEvent ->blockTicket());

        uptime = new Uptime();
        uptime.setLabelToUpdate(lblUptime);

        loadPriceListItem();
    }

    /**
     * Getter.
     * @return Zwraca panel widoku.
     */
    public JPanel getPanel() {
        return panelMain;
    }

    /**
     * Getter.
     * @return Zwraca id pozycji słownika cennika.
     */
    private int getId(){
        String selectedPriceItem = boxSelectPriceList.getSelectedItem().toString();
        Integer priceListId = Integer.parseInt(selectedPriceItem.replaceAll("\\..*", ""));
        return priceListId;
    }

    /**
     * Metoda obsługujaca drukowanie nowych biletów.
     * Sprawdza czy wybrano cennik z listy oraz czy podana liczba punktów jest daną liczbową.
     * @see Tickets::getPrice()
     * @see Tickets::getPriceListItemId()
     * @see Tickets::newTicket()
     */
    private void createNewTicket() {
        if (boxSelectPriceList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano cennika z listy.");
            return;
        }
        String points = txtTicketPoints.getText();
        int id = getId();
        double price = tickets.getPrice(id);
        int priceListItemId = tickets.getPriceListItemId(id);
        if(!points.matches(onlyNumbersRegEx)|| points.equals("")){
            JOptionPane.showMessageDialog(null,"Niedozwolone dane wejściowe. Liczba punktów powinna być liczbą!");
            return;
        }
        else{
            double cost = price * Integer.parseInt(points);
            int response= JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz wydrukować bilet? \n Koszt: " + cost + " zł","Confirm",JOptionPane.YES_NO_OPTION);
            if(response==JOptionPane.YES_OPTION) {
                int ticketNumber = tickets.newTicket(points, priceListItemId);
                JOptionPane.showMessageDialog(null, "Zakupiono bilet. \n Numer biletu: " + ticketNumber + "\n Liczba punktów: " + points);
                txtTicketPoints.setText(null);
                boxSelectPriceList.setSelectedIndex(-1);
                checkNewTicket.setSelected(false);
            }
            else{return;}
        }
    }

    /**
     * Metoda obsługująca doładowywanie biletów.
     * Sprawdza:
     *  -stan wybranego biletu (czy nie został on już zablokowany)
     *  -czy podany bilet istnieje w bazie
     *  -czy liczba punktów oraz numer biletu są liczbą
     * @see Tickets::getPrice()
     * @see Tickets::getPriceListItemId()
     * @see Tickets::checkTicketParameters()
     * @see Tickets::newTopUpTicket()
     */
    private void topUpTicket() {
        if (boxSelectPriceList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(null, "Nie wybrano cennika z listy.");
            return;
        }
        String ticketNo = txtTicketNo.getText();
        String points = txtTicketPoints.getText();
        int id = getId();
        double price = tickets.getPrice(id);
        int priceListItemId = tickets.getPriceListItemId(id);
        if(!points.matches(onlyNumbersRegEx)|| points.equals("")){
            JOptionPane.showMessageDialog(null,"Niedozwolone dane wejściowe. Liczba punktów powinna być liczbą!");
            return;
        }
        if(!ticketNo.matches(onlyNumbersRegEx)|| ticketNo.equals("")){
            JOptionPane.showMessageDialog(null,"Niedozwolone dane wejściowe. Numer biletu powinien być liczbą!");
            return;
        }
        if(!tickets.checkTicketParameters(ticketNo)){
            JOptionPane.showMessageDialog(null,"Bilet o takim numerze nie istnieje lub jest zablokowany!");
            return;
        }
        else{
            double cost = price * Integer.parseInt(points);
            int response= JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz wydrukować bilet? \n Koszt: " + cost + " zł","Confirm",JOptionPane.YES_NO_OPTION);
            if(response==JOptionPane.YES_OPTION) {
                int amountOfPoints = tickets.newTopUpTicket(ticketNo, points, priceListItemId);
                JOptionPane.showMessageDialog(null, "Doładowano bilet. \n Numer biletu: " + ticketNo + "\n Aktualny stan punktów: " + amountOfPoints);
                txtTicketPoints.setText(null);
                boxSelectPriceList.setSelectedIndex(-1);
                checkNewTicket.setSelected(false);
            }
        }
    }

    /**
     * Metoda sprawdzająca czy stan Checkboxa i ustawiająca odpowiednie parametry pól tekstowych i przycisków w GUI.
     * @param e Obiekt sprawdzający czy zaszła jakaś akcja w Checkboxie.
     */
    private void newTicketSlot(ItemEvent e) {
            if(e.getStateChange()==ItemEvent.SELECTED){
                btnPrintTicket.setEnabled(true);
                txtTicketNo.setEnabled(false);
                btnTopUp.setEnabled(false);
                String out=tickets.ticketNoIncrement();
                txtTicketNo.setText(out);
            }
            else{
                txtTicketNo.setText(null);
                txtTicketNo.setEnabled(true);
                btnTopUp.setEnabled(true);
                btnPrintTicket.setEnabled(false);
            }
        }

    /**
     * Metoda odpowiadająca za blokowanie istniejącego biletu.
     * @see Tickets::blockTicket()
     */
    private void blockTicket(){
    int response=JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zablokować bilet","Potwierdzenie",JOptionPane.YES_NO_OPTION);
    if(response==JOptionPane.YES_OPTION) {
        String ticketnumber = txtDeleteTicketNo.getText();
        tickets.blockTicket(ticketnumber);
    }
    else{return;}
}

    /**
     * Metoda odpowiadająca za wybranie z Comboboxa odpowiedniej danej.
     * @see Tickets::getPriceListItem()
     */
    private void loadPriceListItem (){

        ArrayList listItem = tickets.getPriceListItem();
        boxSelectPriceList.setModel(new DefaultComboBoxModel(listItem.toArray()));
        boxSelectPriceList.setSelectedIndex(-1);
    }
}
