package com.habittracker.view;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Login / Register screen with FlatLaf dark styling.
 */
public class LoginView extends JFrame {

    private final JTextField     usernameField;
    private final JPasswordField passwordField;
    private final JButton        loginButton;
    private final JButton        registerButton;

    public LoginView() {
        setTitle("Habit Tracker - Login");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Root panel ──────────────────────────────────────────────
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title
        JLabel title = new JLabel("Welcome!", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        root.add(title, gbc);

        // Username row
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        root.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        usernameField = new JTextField(18);
        root.add(usernameField, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        root.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        passwordField = new JPasswordField(18);
        root.add(passwordField, gbc);

        // Buttons
        loginButton    = makeButton("Login",    new Color(70, 130, 200));
        registerButton = makeButton("Register", new Color(85, 85, 95));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(loginButton);
        btnPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        root.add(btnPanel, gbc);

        // Allow Enter key to trigger login
        passwordField.addActionListener(e -> loginButton.doClick());
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(120, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            final Color original = bg;
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(original.brighter()); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(original); }
        });
        return btn;
    }

    // ── Accessors ────────────────────────────────────────────────────
    public String   getUsername() { return usernameField.getText(); }
    public String   getPassword() { return new String(passwordField.getPassword()); }
    public JButton  getLoginButton()    { return loginButton; }
    public JButton  getRegisterButton() { return registerButton; }
}