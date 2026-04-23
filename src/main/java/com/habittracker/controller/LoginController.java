package com.habittracker.controller;

import com.habittracker.database.DatabaseManager;
import com.habittracker.model.User;
import com.habittracker.view.DashboardView;
import com.habittracker.view.LoginView;

import javax.swing.*;

/**
 * Handles login and registration actions from LoginView.
 */
public class LoginController {

    private final LoginView      view;
    private final DatabaseManager dbManager;

    public LoginController(LoginView view, DatabaseManager dbManager) {
        this.view      = view;
        this.dbManager = dbManager;

        view.getLoginButton()   .addActionListener(e -> login());
        view.getRegisterButton().addActionListener(e -> register());
    }

    private void login() {
        String username = view.getUsername().trim();
        String password = view.getPassword().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Username and password cannot be empty.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate in a SwingWorker so the UI doesn't freeze
        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() {
                return dbManager.validateUser(username, password);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        view.dispose();
                        DashboardView dash = new DashboardView();
                        new DashboardController(dash, dbManager, user);
                        dash.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(view,
                                "Invalid username or password.", "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view,
                            "Login error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void register() {
        String username = view.getUsername().trim();
        String password = view.getPassword().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Username and password cannot be empty.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return dbManager.createUser(username, password);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(view,
                                "Registration successful! Please log in.", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(view,
                                "Username already exists or a database error occurred.",
                                "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view,
                            "Registration error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
