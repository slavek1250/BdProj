package com.bdproj.ski_lift;

import com.bdproj.db.MySQLConnParams;
import com.bdproj.db.MySQLConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Klasa GUI służącego do rejestracji użyć wyciągów.
 * @see SkiLiftUse
 */
public class SkiLiftWin extends SkiLiftUse {

    private JPanel panelMain;           /**< Panel główny. */
    private JTextField txtTicketId;     /**< Pole tekstowe służące do wprowadzania numer id biletu. */
    private JComboBox boxSelectSkiLift; /**< ComboBox służący do wyboru wyciągu. */
    private JButton btnUseSkiLift;      /**< Przycisk odpowiedzialny ze obsługę użycia wyciągu. */
    /**
     * Zmienna przechowywująca wymiary okna.
     */
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    /**
     * Wyrażenie regularne do walidacji formatu wprowadzonego numeru id biletu.
     */
    private final String ONLY_NUMBER_REGEX = "^(?!(0))[0-9]{0,}$";

    /**
     * Domyślny konstruktor.
     */
    private SkiLiftWin() {

        loadSkiLifts();
        boxSelectSkiLift.setSelectedIndex(-1);
        btnUseSkiLift.addActionListener(actionEvent -> useSelectedSkiLift());
    }

    /**
     * Metoda zwracająca obecnie wybrany z listy wyciąg.
     * @return Numer id obecnie wybranego wyciągu. Jeżeli żaden wyciąg nie zaostał wybrany zwraca -1.
     */
    private Integer getSelectedSkiLiftId() {
        if(boxSelectSkiLift.getSelectedIndex() != -1) {
            String selectedSkiLift = boxSelectSkiLift.getSelectedItem().toString();
            return Integer.parseInt(selectedSkiLift.replaceAll("\\..*", ""));
        }
        return -1;
    }

    /**
     * Metoda odświeżająca listę wyciągów.
     */
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

    /**
     * Metoda pobierająca najnowszą listę wyciągów do listy w ComboBox.
     */
    private void loadSkiLifts() {
        if(!super.fetchSkiLifts()) {
            JOptionPane.showMessageDialog(panelMain, super.getLastError());
            return;
        }
        ArrayList<String> skiLifts = new ArrayList<>();
        super.skiLiftsList
                .stream()
                .map(
                    lift -> (lift.get(SkiLiftsListEnum.SKI_LIFT_ID) + ". " + lift.get(SkiLiftsListEnum.NAME))
                )
                .forEach(skiLifts::add);
        boxSelectSkiLift.setModel(new DefaultComboBoxModel(skiLifts.toArray()));
    }

    /**
     * Metoda odpowiedzialna za obsługę użycia wyciągu.
     * Waliduje:
     *      - Poprawność wprowadzonego numeru biletu.
     *      - Istnienie biletu w bazie.
     *      - Czy bilet posiada wystarczającą liczbę punktów do skorzystania z wyciągu.
     */
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

        Integer selectedSkiLiftPointsCost = super.getSkiLiftPointsCost(selectedSkiLiftId);
        int resp = JOptionPane.showConfirmDialog(
                panelMain,
                "Koszt użycia tego wyciągu to " +
                        selectedSkiLiftPointsCost + (selectedSkiLiftPointsCost == 1 ? " punkt" : " punktów") +
                        ".\nCzy chcesz z niego skorzystać?",
                "Potwierdź",
                JOptionPane.YES_NO_OPTION
        );
        if(resp == JOptionPane.NO_OPTION) {
            return;
        }

        Integer ticketPointAmount = getTicketPointsAmount(ticketId);
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

    /**
     * Punkt wejściowy programu, statyczna metoda main. Inicalizuje program.
     * @param args Argumenty uruchomieniowe programu.
     */
    public static void main(String args[]) {
        JFrame frame = new JFrame("Bramka Wyciągu");
        frame.setSize(300, 250);
        frame.setLocation(screenSize.width/2-frame.getSize().width/2,screenSize.height/2-frame.getSize().height/2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("Pomoc");
        JMenuItem help = new JMenuItem("Pomoc");
        help.addActionListener(actionEvent -> showHelp());
        help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menu.add(help);
        menubar.add(menu);
        frame.setJMenuBar(menubar);

        frame.setVisible(true);

        if(!MySQLConnection.readConnParamsFromFile()) {
            JOptionPane.showMessageDialog(null, MySQLConnParams.getLastError());
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }

        SkiLiftWin skiLiftWin = new SkiLiftWin();
        frame.setContentPane(skiLiftWin.panelMain);
        frame.setVisible(true);
    }

    /**
     * Metoda wyświetlająca pomoc użytkownika.
     */
    public static void showHelp() {
        try {
            Runtime.getRuntime().exec("hh.exe help/UserManual.chm");
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Niestety, nie można otworzyć pliku pomocy.");
        }
    }
}
