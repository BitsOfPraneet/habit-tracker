package com.habittracker;

import com.habittracker.controller.LoginController;
import com.habittracker.database.DatabaseManager;
import com.habittracker.view.LoginView;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            LoginView view = new LoginView();
            DatabaseManager dbManager = new DatabaseManager();
            new LoginController(view, dbManager);
            view.setVisible(true);
        });
    }
}