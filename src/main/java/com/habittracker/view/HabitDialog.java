package com.habittracker.view;

import com.habittracker.model.Habit;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Dialog for Adding OR Editing a habit.
 * Shows all fields: name, type, goal quantity/units, frequency/target,
 * reminder toggle + time spinner, archive checkbox.
 * Matches the screenshots exactly.
 */
public class HabitDialog extends JDialog {

    // Form controls
    private final JTextField   nameField;
    private final JRadioButton rbSimple;
    private final JRadioButton rbQuantifiable;
    private final JSpinner     goalQtySpinner;
    private final JTextField   goalUnitsField;
    private final JRadioButton rbDaily;
    private final JRadioButton rbWeekly;
    private final JSpinner     freqTargetSpinner;
    private final JCheckBox    reminderCheck;
    private final JSpinner     reminderTimeSpinner;
    private final JCheckBox    archiveCheck;

    private boolean confirmed = false;

    // ---------------------------------------------------------------

    public HabitDialog(Frame owner, String title, Habit existing) {
        super(owner, title, true);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(18, 20, 12, 20));
        setContentPane(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(4, 6, 4, 6);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        int row     = 0;

        // ── Name ────────────────────────────────────────────────────
        label(panel, "Name:", gbc, 0, row);
        nameField = new JTextField(existing != null ? existing.getHabitName() : "", 22);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 3; gbc.weightx = 1.0;
        panel.add(nameField, gbc); gbc.weightx = 0; gbc.gridwidth = 1;

        // ── Type ────────────────────────────────────────────────────
        row++;
        label(panel, "Type:", gbc, 0, row);
        rbSimple       = new JRadioButton("Simple (Done/Not Done)");
        rbQuantifiable = new JRadioButton("Quantifiable");
        ButtonGroup bgType = new ButtonGroup();
        bgType.add(rbSimple); bgType.add(rbQuantifiable);
        rbSimple.setSelected(true);
        if (existing != null && "QUANTIFIABLE".equals(existing.getHabitType()))
            rbQuantifiable.setSelected(true);
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        typePanel.setOpaque(false);
        typePanel.add(rbSimple); typePanel.add(rbQuantifiable);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 3;
        panel.add(typePanel, gbc); gbc.gridwidth = 1;

        // ── Goal Quantity ────────────────────────────────────────────
        row++;
        label(panel, "Goal Quantity:", gbc, 0, row);
        goalQtySpinner = new JSpinner(new SpinnerNumberModel(
                existing != null ? existing.getGoalQuantity() : 1, 1, 9999, 1));
        goalQtySpinner.setPreferredSize(new Dimension(80, 26));
        gbc.gridx = 1; gbc.gridy = row;
        panel.add(goalQtySpinner, gbc);

        // ── Goal Units ───────────────────────────────────────────────
        label(panel, "Goal Units:", gbc, 2, row);
        goalUnitsField = new JTextField(existing != null && existing.getGoalUnits() != null
                ? existing.getGoalUnits() : "", 10);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = 1.0;
        panel.add(goalUnitsField, gbc); gbc.weightx = 0;

        // ── Frequency ───────────────────────────────────────────────
        row++;
        label(panel, "Frequency:", gbc, 0, row);
        rbDaily  = new JRadioButton("Daily");
        rbWeekly = new JRadioButton("Weekly");
        ButtonGroup bgFreq = new ButtonGroup();
        bgFreq.add(rbDaily); bgFreq.add(rbWeekly);
        rbDaily.setSelected(true);
        if (existing != null && "WEEKLY".equals(existing.getFrequency()))
            rbWeekly.setSelected(true);
        JPanel freqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        freqPanel.setOpaque(false);
        freqPanel.add(rbDaily); freqPanel.add(rbWeekly);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 3;
        panel.add(freqPanel, gbc); gbc.gridwidth = 1;

        // ── Frequency Target ─────────────────────────────────────────
        row++;
        label(panel, "Frequency Target:", gbc, 0, row);
        freqTargetSpinner = new JSpinner(new SpinnerNumberModel(
                existing != null ? existing.getFrequencyTarget() : 1, 1, 99, 1));
        freqTargetSpinner.setPreferredSize(new Dimension(80, 26));
        gbc.gridx = 1; gbc.gridy = row;
        panel.add(freqTargetSpinner, gbc);

        // ── Reminder ────────────────────────────────────────────────
        row++;
        reminderCheck = new JCheckBox("Enable Reminder");
        if (existing != null) reminderCheck.setSelected(existing.isReminderEnabled());
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(reminderCheck, gbc);

        // Time spinner  (HH:mm)
        SpinnerDateModel timeModel = new SpinnerDateModel();
        reminderTimeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(reminderTimeSpinner, "HH:mm");
        reminderTimeSpinner.setEditor(timeEditor);
        reminderTimeSpinner.setPreferredSize(new Dimension(80, 26));
        reminderTimeSpinner.setEnabled(existing != null && existing.isReminderEnabled());
        if (existing != null && existing.getReminderTime() != null
                && !existing.getReminderTime().isBlank()) {
            try {
                LocalTime lt = LocalTime.parse(existing.getReminderTime(),
                        DateTimeFormatter.ofPattern("HH:mm"));
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, lt.getHour());
                cal.set(java.util.Calendar.MINUTE, lt.getMinute());
                reminderTimeSpinner.setValue(cal.getTime());
            } catch (Exception ignored) {}
        }
        gbc.gridx = 1; gbc.gridy = row;
        panel.add(reminderTimeSpinner, gbc);
        reminderCheck.addActionListener(e ->
                reminderTimeSpinner.setEnabled(reminderCheck.isSelected()));

        // ── Archive ─────────────────────────────────────────────────
        row++;
        archiveCheck = new JCheckBox("Archive this habit");
        if (existing != null) archiveCheck.setSelected(existing.isArchived());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        panel.add(archiveCheck, gbc); gbc.gridwidth = 1;

        // ── Buttons ─────────────────────────────────────────────────
        row++;
        JButton saveBtn   = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(saveBtn); btnPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        gbc.insets = new Insets(14, 6, 4, 6);
        panel.add(btnPanel, gbc);

        saveBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Habit name cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
        nameField.requestFocusInWindow();
    }

    // ── Accessors ────────────────────────────────────────────────────

    public boolean isConfirmed()   { return confirmed; }

    public String getHabitName()   { return nameField.getText().trim(); }

    public String getHabitType()   {
        return rbQuantifiable.isSelected() ? "QUANTIFIABLE" : "SIMPLE";
    }

    public int getGoalQuantity()   { return (Integer) goalQtySpinner.getValue(); }

    public String getGoalUnits()   { return goalUnitsField.getText().trim(); }

    public String getFrequency()   { return rbWeekly.isSelected() ? "WEEKLY" : "DAILY"; }

    public int getFrequencyTarget(){ return (Integer) freqTargetSpinner.getValue(); }

    public boolean isReminderEnabled() { return reminderCheck.isSelected(); }

    public String getReminderTime() {
        if (!reminderCheck.isSelected()) return null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format((java.util.Date) reminderTimeSpinner.getValue());
    }

    public boolean isArchived() { return archiveCheck.isSelected(); }

    // ── Private helpers ──────────────────────────────────────────────

    private void label(JPanel p, String text, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0;
        p.add(new JLabel(text), gbc);
    }
}