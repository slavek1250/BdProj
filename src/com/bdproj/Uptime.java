package com.bdproj;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class Uptime {
    private JLabel lblToUpdate;

    private final long DELAY = 1000L;
    private final long PERIOD = 1000L;

    private TimerTask repeatTask;
    private Timer timer;
    int time = 0;

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

    public void setLabelToUpdate(JLabel lbl) {
        lblToUpdate = lbl;
        updateLbl();
    }

    private void updateLbl() {
        long second = time % 60;
        long minute = (time /  60) % 60;
        long hour = (time / (60 * 60)) % 24;

        String timeStr = String.format("%02d:%02d:%02d", hour, minute, second);
        lblToUpdate.setText("Zalogowany od " + timeStr);
    }
}