package com.habittracker.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.net.URL;

public class DashboardView extends JFrame {
    private JTable habitTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, completeButton;
    private JPanel chartPanel;
    private JLabel welcomeLabel;

    public DashboardView() {
        setTitle("Habit Tracker Dashboard");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setBackground(new Color(70, 130, 180));
        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel);
        String[] columnNames = {"ID", "Habit Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        habitTable = new JTable(tableModel);
        habitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitTable.setRowHeight(30);
        habitTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTableHeader tableHeader = habitTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(new Color(242, 242, 242));
        habitTable.getColumnModel().getColumn(0).setMinWidth(0);
        habitTable.getColumnModel().getColumn(0).setMaxWidth(0);
        JScrollPane scrollPane = new JScrollPane(habitTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addButton = new JButton("Add", createIcon("/add.png"));
        editButton = new JButton("Edit", createIcon("/edit.png"));
        deleteButton = new JButton("Delete", createIcon("/delete.png"));
        completeButton = new JButton("Mark Today as Complete");
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 12);
        for (JButton btn : new JButton[]{addButton, editButton, deleteButton, completeButton}) {
            btn.setFont(buttonFont);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(completeButton);
        JPanel controlsAndTablePanel = new JPanel(new BorderLayout());
        controlsAndTablePanel.add(scrollPane, BorderLayout.CENTER);
        controlsAndTablePanel.add(buttonPanel, BorderLayout.SOUTH);
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Weekly Progress"));
        chartPanel.setPreferredSize(new Dimension(380, 0));
        setLayout(new BorderLayout(10, 0));
        add(headerPanel, BorderLayout.NORTH);
        add(controlsAndTablePanel, BorderLayout.CENTER);
        add(chartPanel, BorderLayout.EAST);
    }

    private ImageIcon createIcon(String path) {
        URL url = getClass().getResource(path);
        if (url != null) { return new ImageIcon(url); }
        System.err.println("Couldn't find file: " + path);
        return null;
    }

    public void setWelcomeMessage(String name) { welcomeLabel.setText("Welcome, " + name + "!"); }
    public JTable getHabitTable() { return habitTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getAddButton() { return addButton; }
    public JButton getEditButton() { return editButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getCompleteButton() { return completeButton; }
    public JPanel getChartPanel() { return chartPanel; }
}