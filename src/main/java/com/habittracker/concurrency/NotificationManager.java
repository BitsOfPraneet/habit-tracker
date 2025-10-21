package com.habittracker.concurrency;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationManager {
    private TrayIcon trayIcon;

    public NotificationManager() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported on this system.");
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/add.png"));
        trayIcon = new TrayIcon(image, "Habit Tracker");
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added to the system tray.");
            e.printStackTrace();
        }
    }

    public void start() {
        if (trayIcon == null) {
            return;
        }
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable notificationTask = this::sendNotification;
        scheduler.scheduleAtFixedRate(notificationTask, 2, 2, TimeUnit.HOURS);
    }

    private void sendNotification() {
        trayIcon.displayMessage("Habit Tracker Reminder", "Don't forget to complete your habits for today!", TrayIcon.MessageType.INFO);
    }
}