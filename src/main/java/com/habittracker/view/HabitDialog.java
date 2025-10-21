package com.habittracker.view;

import javax.swing.*;
import java.awt.*;

public class HabitDialog extends JDialog {
    private JTextField habitNameField;
    private boolean confirmed = false;

    public HabitDialog(Frame owner, String title, String initialValue) {
        super(owner, title, true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Habit Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        habitNameField = new JTextField(initialValue, 25);
        panel.add(habitNameField, gbc);
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(15, 0, 0, 0);
        panel.add(buttonPanel, gbc);
        okButton.addActionListener(e -> {
            if (getHabitName().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Habit name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            confirmed = true;
            setVisible(false);
        });
        cancelButton.addActionListener(e -> setVisible(false));
        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }
    public boolean isConfirmed() { return confirmed; }
    public String getHabitName() { return habitNameField.getText(); }
}