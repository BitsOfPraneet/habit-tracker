package com.habittracker.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Main application window (post-login).
 *
 * Left column layout:
 *   - Active habits table (fills remaining space)
 *   - Collapsible "Archived Habits" section with [Unarchive] button
 */
public class DashboardView extends JFrame {

    // ── Active habit table ───────────────────────────────────────────
    private final DefaultTableModel tableModel;
    private final JTable            habitTable;

    // ── Archived habit table ─────────────────────────────────────────
    private final DefaultTableModel archivedTableModel;
    private final JTable            archivedTable;
    private final JButton           unarchiveButton;

    // ── Collapse toggle ──────────────────────────────────────────────
    private final JButton  toggleArchiveBtn;
    private final JPanel   archivedBodyPanel;
    private boolean        archivePanelExpanded = false;

    // ── Analytics panes ──────────────────────────────────────────────
    private final StatsPanel       statsPanel;
    private final WeeklyChartPanel chartPanel;

    // ── Toolbar buttons ──────────────────────────────────────────────
    private final JButton addButton;
    private final JButton editButton;
    private final JButton deleteButton;
    private final JButton completeButton;

    // ── Header ───────────────────────────────────────────────────────
    private final JLabel welcomeLabel;

    // ================================================================
    //  Constructor
    // ================================================================

    public DashboardView() {
        setTitle("Habit Tracker Dashboard");
        setSize(1150, 700);
        setMinimumSize(new Dimension(950, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Header ──────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        header.setBackground(new Color(40, 40, 50));
        welcomeLabel = new JLabel("Habit Tracker Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        header.add(welcomeLabel);
        add(header, BorderLayout.NORTH);

        // ── Active habits table ──────────────────────────────────────
        tableModel = new DefaultTableModel(new String[]{"ID", "Habit Name"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        habitTable = new JTable(tableModel);
        styleTable(habitTable);
        JScrollPane activeScroll = new JScrollPane(habitTable);
        activeScroll.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel activeHeader = sectionHeader("My Habits");

        // ── Archived habits table ────────────────────────────────────
        archivedTableModel = new DefaultTableModel(new String[]{"ID", "Habit Name"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        archivedTable = new JTable(archivedTableModel);
        styleTable(archivedTable);
        JScrollPane archivedScroll = new JScrollPane(archivedTable);
        archivedScroll.setPreferredSize(new Dimension(0, 130));
        archivedScroll.setBorder(new EmptyBorder(0, 0, 0, 0));

        unarchiveButton = new JButton("Unarchive");
        unarchiveButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        unarchiveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        unarchiveButton.setPreferredSize(new Dimension(110, 26));

        JPanel archiveBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        archiveBtnRow.setOpaque(false);
        archiveBtnRow.add(unarchiveButton);

        archivedBodyPanel = new JPanel(new BorderLayout(0, 2));
        archivedBodyPanel.setOpaque(false);
        archivedBodyPanel.setBorder(new EmptyBorder(2, 6, 6, 6));
        archivedBodyPanel.add(archivedScroll, BorderLayout.CENTER);
        archivedBodyPanel.add(archiveBtnRow,  BorderLayout.SOUTH);
        archivedBodyPanel.setVisible(false);   // collapsed by default

        // Toggle button (acts as collapsible header)
        toggleArchiveBtn = new JButton("▶  Archived Habits");
        toggleArchiveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toggleArchiveBtn.setHorizontalAlignment(SwingConstants.LEFT);
        toggleArchiveBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(60, 60, 75)),
                new EmptyBorder(5, 8, 5, 8)));
        toggleArchiveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleArchiveBtn.setFocusPainted(false);
        toggleArchiveBtn.addActionListener(e -> toggleArchivePanel());

        // ── Left column assembly ─────────────────────────────────────
        JPanel leftColumn = new JPanel(new BorderLayout(0, 0));
        leftColumn.setOpaque(false);

        JPanel activeSection = new JPanel(new BorderLayout(0, 0));
        activeSection.setOpaque(false);
        activeSection.add(activeHeader,  BorderLayout.NORTH);
        activeSection.add(activeScroll,  BorderLayout.CENTER);

        JPanel archiveSection = new JPanel(new BorderLayout(0, 0));
        archiveSection.setOpaque(false);
        archiveSection.add(toggleArchiveBtn, BorderLayout.NORTH);
        archiveSection.add(archivedBodyPanel, BorderLayout.CENTER);

        leftColumn.add(activeSection,  BorderLayout.CENTER);
        leftColumn.add(archiveSection, BorderLayout.SOUTH);
        leftColumn.setPreferredSize(new Dimension(270, 0));
        leftColumn.setBorder(new EmptyBorder(6, 6, 4, 4));

        // ── Right panels ─────────────────────────────────────────────
        statsPanel = new StatsPanel();
        chartPanel = new WeeklyChartPanel();

        JPanel rightPane = new JPanel(new GridLayout(2, 1, 0, 8));
        rightPane.setOpaque(false);
        rightPane.setBorder(new EmptyBorder(8, 0, 8, 8));
        rightPane.add(statsPanel);
        rightPane.add(chartPanel);

        // ── Centre split ─────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftColumn, rightPane);
        split.setDividerLocation(285);
        split.setDividerSize(4);
        split.setResizeWeight(0.26);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        // ── Bottom toolbar ───────────────────────────────────────────
        addButton      = createBtn("Add",           new Color(60, 140, 60));
        editButton     = createBtn("Edit",          new Color(60, 100, 180));
        deleteButton   = createBtn("Delete",        new Color(180, 60, 60));
        completeButton = createBtn("Log Completion",new Color(80, 80, 95));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnPanel.setBackground(new Color(38, 38, 48));
        btnPanel.add(addButton);
        btnPanel.add(editButton);
        btnPanel.add(deleteButton);
        btnPanel.add(Box.createHorizontalStrut(20));
        btnPanel.add(completeButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // ================================================================
    //  Collapse / expand
    // ================================================================

    private void toggleArchivePanel() {
        archivePanelExpanded = !archivePanelExpanded;
        archivedBodyPanel.setVisible(archivePanelExpanded);
        toggleArchiveBtn.setText((archivePanelExpanded ? "▼" : "▶") + "  Archived Habits");
        revalidate();
        repaint();
    }

    // ================================================================
    //  Private helpers
    // ================================================================

    private void styleTable(JTable t) {
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setReorderingAllowed(false);
        // Hide ID column
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(0).setWidth(0);
    }

    private JLabel sectionHeader(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(50, 50, 65));
        lbl.setBorder(new EmptyBorder(5, 4, 5, 4));
        return lbl;
    }

    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(130, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ================================================================
    //  Accessors
    // ================================================================

    public void setWelcomeMessage(String username) {
        welcomeLabel.setText("Habit Tracker  —  " + username);
    }

    public JTable            getHabitTable()          { return habitTable; }
    public DefaultTableModel  getTableModel()          { return tableModel; }
    public JTable            getArchivedTable()        { return archivedTable; }
    public DefaultTableModel  getArchivedTableModel()  { return archivedTableModel; }
    public JButton           getUnarchiveButton()      { return unarchiveButton; }
    public JButton           getAddButton()            { return addButton; }
    public JButton           getEditButton()           { return editButton; }
    public JButton           getDeleteButton()         { return deleteButton; }
    public JButton           getCompleteButton()       { return completeButton; }
    public StatsPanel        getStatsPanel()           { return statsPanel; }
    public WeeklyChartPanel  getChartPanel()           { return chartPanel; }
}