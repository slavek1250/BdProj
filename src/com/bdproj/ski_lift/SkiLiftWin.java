package com.bdproj.ski_lift;

import com.bdproj.MainWin;
import com.bdproj.SystemUser;

import javax.swing.*;
import java.util.ArrayList;

public class SkiLiftWin extends SkiLiftUse {

    private JPanel panelMain;
    private JTextField txtTicketId;
    private JComboBox boxSelectSkiLift;
    private JButton btnUseSkiLift;

    private final String ONLY_NUMBER_REGEX = "^(?!(0))[0-9]{0,}$";

    public SkiLiftWin() {
        if(!super.fetchSkiLifts()) {
            JOptionPane.showMessageDialog(panelMain, super.getLastError());
        }
        else loadSkiLifts();

        boxSelectSkiLift.setSelectedIndex(-1);

        btnUseSkiLift.addActionListener(actionEvent -> useSelectedSkiLift());
    }

    private Integer getSelectedSkiLiftId() {
        if(boxSelectSkiLift.getSelectedIndex() != -1) {
            String selectedSkiLift = boxSelectSkiLift.getSelectedItem().toString();
            return Integer.parseInt(selectedSkiLift.replaceAll("\\..*", ""));
        }
        return -1;
    }

    private void reloadSkiLifts() {
        Integer selectedSkiLiftId = getSelectedSkiLiftId();
        loadSkiLifts();
        if(selectedSkiLiftId == -1) {
            boxSelectSkiLift.setSelectedIndex(-1);
            return;
        }
        for(int i = 0; i < boxSelectSkiLift.getModel().getSize(); i++) {
            String currentSkiLift = boxSelectSkiLift.getModel().getElementAt(i).toString();
            if(Integer.parseInt(currentSkiLift.replaceAll("\\..*", "")) == selectedSkiLiftId) {
                boxSelectSkiLift.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadSkiLifts() {
        ArrayList<String> skiLifts = new ArrayList<>();
        super.skiLiftsList
                .stream()
                .map(
                    lift -> (lift.get(SkiLiftsListEnum.SKI_LIFT_ID) + ". " + lift.get(SkiLiftsListEnum.NAME) + " - " + lift.get(SkiLiftsListEnum.POINTS) + "pkt")
                )
                .forEach(skiLifts::add);
        boxSelectSkiLift.setModel(new DefaultComboBoxModel(skiLifts.toArray()));
    }

    private void useSelectedSkiLift() {
        reloadSkiLifts();
        if(boxSelectSkiLift.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(panelMain, "Nie wybrano żadnego wyciągu.");
            return;
        }
        String ticketIdStr = txtTicketId.getText();
        if(!ticketIdStr.matches(ONLY_NUMBER_REGEX) || ticketIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(panelMain, "Błędny format numeru karnetu.");
            return;
        }
        Integer selectedSkiLiftId = getSelectedSkiLiftId();
        Integer ticketId = Integer.parseInt(ticketIdStr);

        if(!super.isTicketExisting(ticketId)) {
            JOptionPane.showMessageDialog(panelMain, "Podano numer nieistniejącego biletu.");
            return;
        }

        Integer ticketPointAmount = getTicketPointsAmount(ticketId);
        Integer selectedSkiLiftPointsCost = super.getSkiLiftPointsCost(selectedSkiLiftId);
        if((ticketPointAmount - selectedSkiLiftPointsCost) < 0) {
            JOptionPane.showMessageDialog(panelMain,
                    "Posiadasz " +
                            ticketPointAmount + (ticketPointAmount == 1 ? " punkt" : " punktów") +
                            ".\nNie posiadasz wystarczającej liczby punktów aby skorzystać z tego wyciągu."
            );
            return;
        }

        Integer selectedSkiLiftDataId = getSkiLiftDataId(selectedSkiLiftId);
        if(selectedSkiLiftDataId == -1) {
            JOptionPane.showMessageDialog(panelMain, "Wykryto niespójność danych...");
            return;
        }

        if(!addTicketUse(selectedSkiLiftDataId, ticketId)) {
            JOptionPane.showMessageDialog(panelMain, super.getLastError());
            return;
        }

        ticketPointAmount = getTicketPointsAmount(ticketId);
        JOptionPane.showMessageDialog(panelMain,
                "Skorzystano z wyciągu " +
                        super.getSkiLiftName(selectedSkiLiftId) +
                        ".\nObecnie posiadasz " +
                        ticketPointAmount +
                        (ticketPointAmount == 1 ? " punkt." : " punktów.")
        );

        boxSelectSkiLift.setSelectedIndex(-1);
        txtTicketId.setText("");
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame("Bramka Wyciągu");
        SkiLiftWin skiLiftWin = new SkiLiftWin();

        frame.setContentPane(skiLiftWin.panelMain);

        frame.setSize(300, 250);
        //frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}