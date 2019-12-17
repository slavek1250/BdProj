package com.bdproj;

import javafx.event.Event;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
import java.util.ArrayList;

public class EmployeeWgt extends Employee {
    private JPanel panelMain;
    private JButton btnLogout;
    private JTextField txtTicketNo;
    private JCheckBox checkNewTicket;
    private JTextField txtTicketPoints;
    private JComboBox boxSelectPriceList;
    private JButton btnPrintTicket;
    private JButton btnTopUp;
    private JTextField txtDeleteTicketNo;
    private JButton btnDeleteTicket;
    private JLabel lblHello;

    private MainView mainView;
    // TODO: Migracja kodu tworzenie biletow do klasy Tickets.#Karol# !!DONE!!

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

        loadPriceListItem();
    }

    public JPanel getPanel() {
        return panelMain;
    }

    private void createNewTicket() {
        if(!validateTopUpNewTicketData()) return;

        // dodanie nowego biletu do bazy

        topUpTicket();
    }

    private void topUpTicket() {
        if(!validateTopUpNewTicketData()) return;

        String selectedPriceListDictionary= boxSelectPriceList.getSelectedItem().toString();
        Integer priceListDictionaryId = Integer.parseInt(selectedPriceListDictionary.replaceAll("\\..*", ""));
        JOptionPane.showMessageDialog(panelMain, "Id pozycji cennika: " + tickets.getPriceListItemId(priceListDictionaryId) + " cena: " + tickets.getPrice(priceListDictionaryId));

        Double unitPrice = tickets.getPrice(priceListDictionaryId);
        Integer priceListItemId = tickets.getPriceListItemId(priceListDictionaryId);

        // doładowanie biletu, wyświelenie kwoty itd...
    }

    private boolean validateTopUpNewTicketData() {
        // walidajca
        return true;
    }

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
private void blockTicket(){
    int response=JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zablokować bilet","Potwierdzenie",JOptionPane.YES_NO_OPTION);
    if(response==JOptionPane.YES_OPTION) {
        String ticketnumber = txtDeleteTicketNo.getText();
        tickets.blockTicket(ticketnumber);
    }
    else{return;}
}

    private void loadPriceListItem (){

        ArrayList listItem = tickets.getPriceListItem();
        boxSelectPriceList.setModel(new DefaultComboBoxModel(listItem.toArray()));
        boxSelectPriceList.setSelectedIndex(-1);
    }
}
