package com.bdproj.mysqlserver;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Klasa GUI, nakładka na konsolową aplikację serwera.
 */
public class ServerWgt {
    private JPanel mainPanel; /**< Panel główny. */
    private JTextArea txtArea; /**< Pole tekstowe do wyświetlania informacji drukowanych w konsoli. */
    private JButton btnStartStop; /**< Przycisk włączający / wyłączający serwer. */
    private JScrollPane scrollPane; /**< ScrollArea. */

    private final long DELAY = 50L; /**< Przerwa między odczytem z konsoli. */
    private final long PERIOD = 50L; /**< Okres odczytów danych z konsoli. */

    private Runtime run;    /**< Objekt runtime serwera. */
    private Process serverProcess = null; /**< Proces serwera. */
    private BufferedReader bufferedReader; /**< Bufor odczytu danych z konsoli. */

    /**
     * Domyślny kostruktor.
     */
    ServerWgt() {

        run  = Runtime.getRuntime();
        txtArea.setEditable(false);
        btnStartStop.addActionListener(actionEvent -> startStop());

        run.addShutdownHook(new Thread(this::stopServer));

        TimerTask repeatTask = new TimerTask() {
            @Override
            public void run() {
                readServerOutput();
            }
        };
        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(repeatTask, DELAY, PERIOD);
    }

    /**
     * Metoda odczytująca dane z konsoli.
     */
    private void readServerOutput() {
        if(isServerRunning()) {
            String line;
            try {
                if ((line = bufferedReader.readLine()) != null) {
                    txtArea.append(line + "\n");
                    scrollDown();
                }
            }
            catch (IOException ex) {
                txtArea.append(ex.getMessage() + "\n");
                scrollDown();
            }
        }
    }

    /**
     * Metoda włączająca / wyłączająca serwer.
     */
    private void startStop() {
        if(isServerRunning()) {
            txtArea.setText("");
            btnStartStop.setText("Start server");
            stopServer();
        }
        else {
            runServer();
            btnStartStop.setText("Stop server");
        }
    }

    /**
     * Metoda przewijająca w dół pole tekstowe.
     */
    private void scrollDown() {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
    }

    /**
     * Metoda zatrzymująca serwer.
     */
    private void stopServer() {
        if(isServerRunning()) {
            serverProcess.destroy();
            serverProcess = null;
        }
    }

    /**
     * Metoda sprawdzająca czy serwer jest włączony.
     * @return Zwraca true jeżeli serwer jest włączony.
     */
    private boolean isServerRunning() {
        return (serverProcess != null);
    }

    /**
     * Getter.
     * @return Panel GUI.
     */
    JPanel getMainPanel(){
        return mainPanel;
    }

    /**
     * Metoda włączająca serwer.
     */
    private void runServer() {

        try {
            serverProcess = run.exec("server/bin/mysqld.exe --defaults-file=server/mysql.ini --console");
            bufferedReader = new BufferedReader(new InputStreamReader(serverProcess.getErrorStream()));
        }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage());
        }
    }
}
