package com.bdproj;

import javafx.event.Event;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
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
    // TODO: Migracja kodu tworzenie biletow do klasy Tickets.

    public EmployeeWgt(MainView mainView, SystemUser user) {
        super(user);
        this.mainView = mainView;

        lblHello.setText("Witaj, " + systemUser.getName() + "!");

        btnPrintTicket.setEnabled(false);

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mainView.showMainView();
            }
        });

        checkNewTicket.addItemListener(e -> newTicketSlot(e));

        btnDeleteTicket.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                PreparedStatement ps;
                ResultSet rs;
                String ticketnumber= txtDeleteTicketNo.getText();
                String query ="SELECT zablokowany FROM karnet WHERE id=?";

                try {
                    ps = MySQLConnection.getConnection().prepareStatement(query);
                    ps.setString(1,ticketnumber);
                    rs= ps.executeQuery();
                    if(rs.first()) {
                        int zab = rs.getInt("zablokowany");
                        if (zab == 1) {
                            JOptionPane.showMessageDialog(null, "Ten bilet jest już zablokowany");
                        }
                        else{
                            String query1="UPDATE karnet SET zablokowany=1 WHERE id=?";
                            PreparedStatement ps1 = MySQLConnection.getConnection().prepareStatement(query1);
                            ps1.setString(1,ticketnumber);
                            int rs1= ps1.executeUpdate();
                        JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz zablokować bilet");
                        }
                    }
                    else{
                        JOptionPane.showMessageDialog(null,"Nie ma takiego biletu");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }


            }
        });
    }

    public JPanel getPanel() {
        return panelMain;
    }

    public void newTicketSlot(ItemEvent e) {
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

}
