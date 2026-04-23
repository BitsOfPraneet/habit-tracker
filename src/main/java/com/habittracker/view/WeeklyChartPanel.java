package com.habittracker.view;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * Bar chart panel showing completions over the last 7 days.
 * Styled to match the dark dashboard theme.
 */
public class WeeklyChartPanel extends JPanel {

    private final JPanel chartHolder;

    public WeeklyChartPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Weekly Progress",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12)));
        setOpaque(false);

        chartHolder = new JPanel(new BorderLayout());
        chartHolder.setOpaque(false);
        add(chartHolder, BorderLayout.CENTER);
    }

    /**
     * Refreshes the chart with new daily-count data.
     *
     * @param data  map of date → completion count (may be empty, never null)
     */
    public void update(Map<LocalDate, Integer> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        LocalDate day = LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) {
            String label = day.getDayOfWeek().toString().substring(0, 3);
            dataset.addValue(data.getOrDefault(day, 0), "Completions", label);
            day = day.plusDays(1);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null, null, "Count", dataset);

        // ── Cosmetics ───────────────────────────────────────────────
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(45, 45, 55));
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(new Color(70, 70, 80));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(255, 80, 80));
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());

        // axis colours
        plot.getDomainAxis().setTickLabelPaint(Color.LIGHT_GRAY);
        plot.getDomainAxis().setAxisLinePaint(Color.GRAY);
        plot.getRangeAxis().setTickLabelPaint(Color.LIGHT_GRAY);
        plot.getRangeAxis().setAxisLinePaint(Color.GRAY);
        plot.getRangeAxis().setStandardTickUnits(
                org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());

        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(false);
        cp.setPopupMenu(null);
        cp.setOpaque(false);

        chartHolder.removeAll();
        chartHolder.add(cp, BorderLayout.CENTER);
        chartHolder.revalidate();
        chartHolder.repaint();
    }
}
