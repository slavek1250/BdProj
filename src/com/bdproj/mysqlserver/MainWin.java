package com.bdproj.mysqlserver;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Klasa główna programu.
 */
public class MainWin extends JFrame {

    /**
     * Konstruktor domyślny.
     * @param title Tytuł okna.
     */
    public MainWin(String title) {
        super(title);
    }

    /**
     * Metoda główna programu. Puknt wejściowy programu.
     * @param args
     */
    public static void main(String[] args) {
        JFrame frame = new MainWin("MySQL Server 5.6.46 Runner ");
        ServerWgt mainWin = new ServerWgt();

        frame.setContentPane(mainWin.getMainPanel());

        frame.setSize(700, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
