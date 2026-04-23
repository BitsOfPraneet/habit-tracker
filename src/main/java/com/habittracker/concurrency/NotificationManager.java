package com.habittracker.concurrency;
import com.habittracker.database.DatabaseManager;
import com.habittracker.model.Habit;

import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;
/**
 * Checks every minute whether any reminder time matches the current HH:mm
 * for any habit owned by the given user, then shows a system-tray or
 * dialog notification (falls back to JOptionPane if tray is not supported).
 */
public class NotificationManager {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final int            userId;
    private final DatabaseManager dbManager;
    private ScheduledExecutorService scheduler;
    private java.awt.TrayIcon trayIcon;

    public NotificationManager(int userId, DatabaseManager dbManager) {
        this.userId    = userId;
        this.dbManager = dbManager;
        initTray();
    }

    private void initTray() {
        if (!java.awt.SystemTray.isSupported()) return;
        java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
        // Use a simple solid-colour image as the tray icon (no external file needed)
        java.awt.Image img = new java.awt.image.BufferedImage(16, 16,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = ((java.awt.image.BufferedImage) img).createGraphics();
        g.setColor(new java.awt.Color(70, 160, 100));
        g.fillOval(0, 0, 15, 15);
        g.dispose();

        trayIcon = new java.awt.TrayIcon(img, "Habit Tracker");
        trayIcon.setImageAutoSize(true);
        try { tray.add(trayIcon); } catch (java.awt.AWTException ignored) {
            trayIcon = null;
        }
    }
    /** Starts the background 1-minute polling thread. */
    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NotificationManager");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::checkReminders, 0, 1, TimeUnit.MINUTES);
    }

    /** Gracefully shuts down the background thread (call on app exit). */
    public void stop() {
        if (scheduler != null) scheduler.shutdownNow();
        if (trayIcon != null) {
            java.awt.SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    // ----------------------------------------------------------------

    private void checkReminders() {
        String nowHHmm = LocalTime.now().format(HH_MM);
        List<Habit> remindable = dbManager.getHabitsWithReminders(userId);
        for (Habit h : remindable) {
            if (nowHHmm.equals(h.getReminderTime())) {
                String msg = "Time to complete: '" + h.getHabitName() + "'!";
                notify(h.getHabitName(), msg);
            }
        }
    }

    private void notify(String title, String msg) {
        if (trayIcon != null) {
            trayIcon.displayMessage("Habit Tracker — " + title, msg,
                    java.awt.TrayIcon.MessageType.INFO);
        } else {
            // Fallback: show a non-blocking dialog on the EDT
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, msg,
                        "Habit Reminder", JOptionPane.INFORMATION_MESSAGE));
        }
    }
}
