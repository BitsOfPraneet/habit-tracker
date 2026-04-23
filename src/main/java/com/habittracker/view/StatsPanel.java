package com.habittracker.view;

import com.habittracker.model.HabitStats;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

/**
 * Right-hand panel shown when a habit is selected.
 * Displays: Current/Longest Streak, Completion Rate, and the Heatmap calendar.
 */
public class StatsPanel extends JPanel {

    private final JLabel currentStreakLabel  = new JLabel("—");
    private final JLabel longestStreakLabel  = new JLabel("—");
    private final JLabel completionRateLabel = new JLabel("—");
    private final HeatmapPanel heatmap       = new HeatmapPanel();

    public StatsPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ── Streak / rate stats row ──────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 10, 0));
        statsRow.setOpaque(false);
        statsRow.add(statCard("Current Streak", currentStreakLabel));
        statsRow.add(statCard("Longest Streak", longestStreakLabel));
        statsRow.add(statCard("Completion Rate", completionRateLabel));
        add(statsRow, BorderLayout.NORTH);

        // ── Heatmap ─────────────────────────────────────────────────
        JPanel heatmapWrapper = new JPanel(new BorderLayout());
        heatmapWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Completion Heatmap",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12)));
        heatmapWrapper.setOpaque(false);
        heatmapWrapper.add(heatmap, BorderLayout.CENTER);
        add(heatmapWrapper, BorderLayout.CENTER);
    }

    /** Updates stats labels and heatmap from freshly fetched data. */
    public void update(HabitStats stats, Set<LocalDate> completedDates) {
        currentStreakLabel .setText(stats.getCurrentStreak()   + " days");
        longestStreakLabel .setText(stats.getLongestStreak()   + " days");
        completionRateLabel.setText(String.format("%.1f%%", stats.getCompletionRate()));
        heatmap.setData(completedDates);
    }

    /** Clears all stats (called when nothing is selected). */
    public void clear() {
        currentStreakLabel .setText("—");
        longestStreakLabel .setText("—");
        completionRateLabel.setText("—");
        heatmap.setData(null);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private JPanel statCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(8, 12, 8, 12)));
        card.setOpaque(false);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ================================================================
    //  Inner class: Heatmap
    // ================================================================

    private static class HeatmapPanel extends JPanel {

        private Set<LocalDate> completedDates;

        HeatmapPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 160));
        }

        void setData(Set<LocalDate> dates) {
            this.completedDates = dates;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (completedDates == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cellSize = 14;
            int gap      = 2;
            int step     = cellSize + gap;
            int startX   = 30;   // leave room for day labels
            int startY   = 20;   // leave room for month labels

            // Draw day-of-week labels (Sun–Sat abbreviated)
            String[] days = {"S","M","T","W","T","F","S"};
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (int d = 0; d < 7; d++) {
                g2.setColor(UIManager.getColor("Label.foreground") != null
                        ? UIManager.getColor("Label.foreground") : Color.GRAY);
                g2.drawString(days[d], 14, startY + d * step + cellSize - 3);
            }

            // We draw the last 52 weeks (364 days) + partial current week
            LocalDate today = LocalDate.now();
            // Start on the most recent Sunday that is >= 364 days ago
            LocalDate start = today.minusDays(363);
            // Align to Sunday
            while (start.getDayOfWeek().getValue() % 7 != 0) {
                start = start.minusDays(1);
            }

            // Color palette
            Color empty = new Color(55, 55, 65);
            Color fill  = new Color(70, 160, 100);

            LocalDate cursor = start;
            int col = 0;
            String lastMonth = "";
            while (!cursor.isAfter(today)) {
                int dow = cursor.getDayOfWeek().getValue() % 7; // Sun=0
                if (dow == 0) col++;                           // new column per week

                // Month header
                String monthAbbr = cursor.getMonth().toString().substring(0, 3);
                if (dow == 0 && !monthAbbr.equals(lastMonth)) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                    g2.setColor(Color.GRAY);
                    g2.drawString(monthAbbr, startX + (col - 1) * step, startY - 5);
                    lastMonth = monthAbbr;
                }

                int x = startX + (col - 1) * step;
                int y = startY + dow * step;
                boolean done = completedDates.contains(cursor);
                g2.setColor(done ? fill : empty);
                g2.fillRoundRect(x, y, cellSize, cellSize, 3, 3);

                cursor = cursor.plusDays(1);
            }
        }
    }
}
