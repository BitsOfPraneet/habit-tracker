package com.habittracker;

import com.formdev.flatlaf.FlatDarkLaf;
import com.habittracker.database.DatabaseManager;
import com.habittracker.view.LoginView;
import com.habittracker.controller.LoginController;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Install FlatLaf dark L&F before any Swing component is created
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            // Fallback gracefully to Nimbus
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
            catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            LoginView      view      = new LoginView();
            DatabaseManager dbManager = new DatabaseManager();
            new LoginController(view, dbManager);
            view.setVisible(true);
        });
    }
}