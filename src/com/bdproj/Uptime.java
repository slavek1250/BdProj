package com.bdproj;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Klasa zliczająca czas od zalogowania.
 */
public class Uptime {
    private JLabel lblToUpdate;         /**< Etykieta do wyświetlania zliczonego czasu. */

    private final long DELAY = 1000L;   /**< Opóźnienie dla timera. */
    private final long PERIOD = 1000L;  /**< Interwał wyzwalania timera. */

    private TimerTask repeatTask;       /**< Obiekt zadania timera. */
    private Timer timer;                /**< Obiekt timera. */
    int time = 0;                       /**< Zliczone sekundy */

    /**
     * Domyślny konstruktor.
     */
    public Uptime() {
        repeatTask = new TimerTask() {
            @Override
            public void run() {
                time++;
                updateLbl();
            }
        };
        timer = new Timer("Timer");
        timer.scheduleAtFixedRate(repeatTask, DELAY, PERIOD);
    }

    /**
     * Setter.
     * @param lbl Etykieta do wyświetlania zliczonego czasu.
     */
    public void setLabelToUpdate(JLabel lbl) {
        lblToUpdate = lbl;
        updateLbl();
    }

    /**
     * Metoda aktualizująca wyświtalnie zliczonego czasu.
     */
    private void updateLbl() {
        long second = time % 60;
        long minute = (time /  60) % 60;
        long hour = (time / (60 * 60)) % 24;

        String timeStr = String.format("%02d:%02d:%02d", hour, minute, second);
        lblToUpdate.setText("Zalogowany od " + timeStr);
    }
}