package com.habittracker.controller;

import com.habittracker.database.DatabaseManager;
import com.habittracker.model.User;
import com.habittracker.view.DashboardView;
import com.habittracker.view.LoginView;
import javax.swing.JOptionPane;

public class LoginController {
    private final LoginView view;
    private final DatabaseManager dbManager;

    public LoginController(LoginView view, DatabaseManager dbManager) {
        this.view = view;
        this.dbManager = dbManager;
        initController();
    }

    private void initController() {
        view.getLoginButton().addActionListener(e -> login());
        view.getRegisterButton().addActionListener(e -> register());
    }

    private void login() {
        String username = view.getUsername().trim(); // <-- Add .trim()
        String password = view.getPassword().trim(); // <-- Add .trim()
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User user = dbManager.validateUser(username, password);
        if (user != null) {
            view.dispose();
            DashboardView dashboardView = new DashboardView();
            new DashboardController(dashboardView, new DatabaseManager(), user);
            dashboardView.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(view, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
         String username = view.getUsername().trim(); // <-- Add .trim()
         String password = view.getPassword().trim(); // <-- Add .trim()
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (dbManager.createUser(username, password)) {
            JOptionPane.showMessageDialog(view, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view, "Username may already exist or a database error occurred.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
