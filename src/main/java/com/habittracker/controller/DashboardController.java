package com.habittracker.controller;

import com.habittracker.concurrency.NotificationManager;
import com.habittracker.database.DatabaseManager;
import com.habittracker.model.Habit;
import com.habittracker.model.User;
import com.habittracker.view.DashboardView;
import com.habittracker.view.HabitDialog;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardController {
    private final DashboardView view;
    private final DatabaseManager dbManager;
    private final User currentUser;

    public DashboardController(DashboardView view, DatabaseManager dbManager, User user) {
        this.view = view;
        this.dbManager = dbManager;
        this.currentUser = user;
        initController();
    }

    private void initController() {
        view.setWelcomeMessage(currentUser.getUsername());
        view.getAddButton().addActionListener(e -> addHabit());
        view.getEditButton().addActionListener(e -> editHabit());
        view.getDeleteButton().addActionListener(e -> deleteHabit());
        view.getCompleteButton().addActionListener(e -> markComplete());
        new NotificationManager().start();
        loadHabits();
        updateChart();
    }

    private void loadHabits() {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        List<Habit> habits = dbManager.getHabitsForUser(currentUser.getUserId());
        for (Habit habit : habits) {
            model.addRow(new Object[]{habit.getHabitId(), habit.getHabitName()});
        }
    }

    private void addHabit() {
        HabitDialog dialog = new HabitDialog(view, "Add New Habit", "");
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Habit newHabit = new Habit(dialog.getHabitName(), currentUser.getUserId());
            if (dbManager.addHabit(newHabit)) {
                loadHabits();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to add habit.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editHabit() {
        int selectedRow = view.getHabitTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a habit to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int habitId = (int) view.getTableModel().getValueAt(selectedRow, 0);
        String habitName = (String) view.getTableModel().getValueAt(selectedRow, 1);
        HabitDialog dialog = new HabitDialog(view, "Edit Habit", habitName);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Habit updatedHabit = new Habit(habitId, dialog.getHabitName(), currentUser.getUserId());
            if (dbManager.updateHabit(updatedHabit)) {
                loadHabits();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update habit.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteHabit() {
        int selectedRow = view.getHabitTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a habit to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int habitId = (int) view.getTableModel().getValueAt(selectedRow, 0);
        int choice = JOptionPane.showConfirmDialog(view, "Are you sure you want to delete this habit and all its logs?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (dbManager.deleteHabit(habitId)) {
                loadHabits();
                updateChart();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete habit.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void markComplete() {
        int selectedRow = view.getHabitTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a habit to mark as complete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int habitId = (int) view.getTableModel().getValueAt(selectedRow, 0);
        if (dbManager.markHabitComplete(habitId, LocalDate.now())) {
            JOptionPane.showMessageDialog(view, "Habit marked as complete for today!", "Success", JOptionPane.INFORMATION_MESSAGE);
            updateChart();
        } else {
            JOptionPane.showMessageDialog(view, "Failed to mark habit. It might already be complete for today.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<LocalDate, Integer> data = dbManager.getCompletionDataForChart(currentUser.getUserId(), 7);
        LocalDate date = LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) {
            int count = data.getOrDefault(date, 0);
            dataset.addValue(count, "Completions", date.getDayOfWeek().toString().substring(0, 3));
            date = date.plusDays(1);
        }
        JFreeChart barChart = ChartFactory.createBarChart("Habits Completed (Last 7 Days)", "Day", "Count", dataset);
        ChartPanel chartPanel = new ChartPanel(barChart);
        view.getChartPanel().removeAll();
        view.getChartPanel().add(chartPanel, BorderLayout.CENTER);
        view.getChartPanel().revalidate();
        view.getChartPanel().repaint();
    }
}
