package com.habittracker.controller;

import com.habittracker.concurrency.NotificationManager;
import com.habittracker.database.DatabaseManager;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitStats;
import com.habittracker.model.User;
import com.habittracker.view.DashboardView;
import com.habittracker.view.HabitDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wires up all dashboard interactions.
 * Every database call is dispatched to a SwingWorker to keep the UI responsive.
 */
public class DashboardController {

    private final DashboardView    view;
    private final DatabaseManager  db;
    private final User             currentUser;
    private final NotificationManager notifManager;

    public DashboardController(DashboardView view, DatabaseManager db, User user) {
        this.view        = view;
        this.db          = db;
        this.currentUser = user;

        notifManager = new NotificationManager(user.getUserId(), db);
        notifManager.start();

        initView();
    }

    // ================================================================
    //  Initialisation
    // ================================================================

    private void initView() {
        view.setWelcomeMessage(currentUser.getUsername());

        // Active-habits toolbar buttons
        view.getAddButton()     .addActionListener(e -> addHabit());
        view.getEditButton()    .addActionListener(e -> editHabit());
        view.getDeleteButton()  .addActionListener(e -> deleteHabit());
        view.getCompleteButton().addActionListener(e -> markComplete());

        // Unarchive button
        view.getUnarchiveButton().addActionListener(e -> unarchiveHabit());

        // Stats update when selecting an active habit
        view.getHabitTable().getSelectionModel()
            .addListSelectionListener(this::onActiveSelectionChanged);

        // Clear stats when selecting an archived habit (no analytics shown for archived)
        view.getArchivedTable().getSelectionModel()
            .addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()
                        && view.getArchivedTable().getSelectedRow() >= 0) {
                    view.getHabitTable().clearSelection();
                    view.getStatsPanel().clear();
                }
            });

        // Shutdown hook
        view.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                notifManager.stop();
            }
        });

        loadHabits();
        loadArchivedHabits();
        refreshWeeklyChart();
    }

    // ================================================================
    //  Async loaders
    // ================================================================

    private void loadHabits() {
        new SwingWorker<List<Habit>, Void>() {
            @Override protected List<Habit> doInBackground() {
                return db.getHabitsForUser(currentUser.getUserId(), false);
            }
            @Override protected void done() {
                try {
                    DefaultTableModel model = view.getTableModel();
                    model.setRowCount(0);
                    for (Habit h : get())
                        model.addRow(new Object[]{h.getHabitId(), h.getHabitName()});
                    view.getStatsPanel().clear();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void loadArchivedHabits() {
        new SwingWorker<List<Habit>, Void>() {
            @Override protected List<Habit> doInBackground() {
                return db.getHabitsForUser(currentUser.getUserId(), true);
            }
            @Override protected void done() {
                try {
                    List<Habit> all = get();
                    DefaultTableModel model = view.getArchivedTableModel();
                    model.setRowCount(0);
                    for (Habit h : all)
                        if (h.isArchived())
                            model.addRow(new Object[]{h.getHabitId(), h.getHabitName()});
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void refreshWeeklyChart() {
        new SwingWorker<Map<LocalDate, Integer>, Void>() {
            @Override protected Map<LocalDate, Integer> doInBackground() {
                return db.getCompletionDataForChart(currentUser.getUserId(), 7);
            }
            @Override protected void done() {
                try { view.getChartPanel().update(get()); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void refreshStatsFor(int habitId) {
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                HabitStats     stats = db.getStatsForHabit(habitId);
                Set<LocalDate> dates = db.getCompletionDatesForHabit(habitId);
                return new Object[]{stats, dates};
            }
            @Override protected void done() {
                try {
                    Object[]       res   = get();
                    HabitStats     stats = (HabitStats) res[0];
                    @SuppressWarnings("unchecked")
                    Set<LocalDate> dates = (Set<LocalDate>) res[1];
                    view.getStatsPanel().update(stats, dates);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ================================================================
    //  Event handlers
    // ================================================================

    private void onActiveSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = view.getHabitTable().getSelectedRow();
        if (row < 0) { view.getStatsPanel().clear(); return; }
        // Deselect archived table
        view.getArchivedTable().clearSelection();
        int habitId = (int) view.getTableModel().getValueAt(row, 0);
        refreshStatsFor(habitId);
    }

    private void addHabit() {
        HabitDialog dlg = new HabitDialog(view, "Add New Habit", null);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;

        Habit h = buildHabitFromDialog(dlg, -1);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return db.addHabit(h); }
            @Override protected void done() {
                try {
                    if (get()) { loadHabits(); loadArchivedHabits(); }
                    else       { showError("Failed to add habit."); }
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private void editHabit() {
        int row = view.getHabitTable().getSelectedRow();
        if (row < 0) { showWarn("Please select an active habit to edit."); return; }
        int habitId = (int) view.getTableModel().getValueAt(row, 0);

        new SwingWorker<Habit, Void>() {
            @Override protected Habit doInBackground() { return db.getHabitById(habitId); }
            @Override protected void done() {
                try {
                    Habit existing = get();
                    if (existing == null) { showError("Could not load habit."); return; }
                    HabitDialog dlg = new HabitDialog(view, "Edit Habit", existing);
                    dlg.setVisible(true);
                    if (!dlg.isConfirmed()) return;

                    Habit updated = buildHabitFromDialog(dlg, habitId);
                    new SwingWorker<Boolean, Void>() {
                        @Override protected Boolean doInBackground() { return db.updateHabit(updated); }
                        @Override protected void done() {
                            try {
                                if (get()) { loadHabits(); loadArchivedHabits(); refreshWeeklyChart(); }
                                else       { showError("Failed to update habit."); }
                            } catch (Exception ex) { showError(ex.getMessage()); }
                        }
                    }.execute();
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private void deleteHabit() {
        int row = view.getHabitTable().getSelectedRow();
        if (row < 0) { showWarn("Please select an active habit to delete."); return; }
        int    habitId = (int)    view.getTableModel().getValueAt(row, 0);
        String name    = (String) view.getTableModel().getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(view,
                "Delete '" + name + "' and all its logs?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return db.deleteHabit(habitId); }
            @Override protected void done() {
                try {
                    if (get()) {
                        loadHabits(); loadArchivedHabits();
                        refreshWeeklyChart();
                        view.getStatsPanel().clear();
                    } else { showError("Failed to delete habit."); }
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    private void markComplete() {
        int row = view.getHabitTable().getSelectedRow();
        if (row < 0) { showWarn("Please select a habit to log."); return; }
        int habitId = (int) view.getTableModel().getValueAt(row, 0);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return db.markHabitComplete(habitId, LocalDate.now());
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(view,
                                "Habit logged successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        refreshWeeklyChart();
                        refreshStatsFor(habitId);
                    } else {
                        JOptionPane.showMessageDialog(view,
                                "Already logged for today, or an error occurred.",
                                "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    /** Unarchives the selected habit from the archived table. */
    private void unarchiveHabit() {
        int row = view.getArchivedTable().getSelectedRow();
        if (row < 0) { showWarn("Please select an archived habit to unarchive."); return; }
        int    habitId = (int)    view.getArchivedTableModel().getValueAt(row, 0);
        String name    = (String) view.getArchivedTableModel().getValueAt(row, 1);

        new SwingWorker<Habit, Void>() {
            @Override protected Habit doInBackground() { return db.getHabitById(habitId); }
            @Override protected void done() {
                try {
                    Habit h = get();
                    if (h == null) { showError("Could not load habit."); return; }
                    h.setArchived(false);
                    new SwingWorker<Boolean, Void>() {
                        @Override protected Boolean doInBackground() { return db.updateHabit(h); }
                        @Override protected void done() {
                            try {
                                if (get()) {
                                    JOptionPane.showMessageDialog(view,
                                            "'" + name + "' has been unarchived.",
                                            "Unarchived", JOptionPane.INFORMATION_MESSAGE);
                                    loadHabits();
                                    loadArchivedHabits();
                                } else {
                                    showError("Failed to unarchive habit.");
                                }
                            } catch (Exception ex) { showError(ex.getMessage()); }
                        }
                    }.execute();
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        }.execute();
    }

    // ================================================================
    //  Helpers
    // ================================================================

    private Habit buildHabitFromDialog(HabitDialog dlg, int habitId) {
        Habit h = new Habit(
            currentUser.getUserId(),
            dlg.getHabitName(),
            dlg.getHabitType(),
            dlg.getGoalQuantity(),
            dlg.getGoalUnits(),
            dlg.getFrequency(),
            dlg.getFrequencyTarget(),
            dlg.isReminderEnabled(),
            dlg.getReminderTime(),
            dlg.isArchived()
        );
        if (habitId > 0) h.setHabitId(habitId);
        return h;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(view, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(view, msg, "No Selection", JOptionPane.WARNING_MESSAGE);
    }
}
