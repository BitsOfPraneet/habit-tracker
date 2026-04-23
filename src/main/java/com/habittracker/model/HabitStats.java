package com.habittracker.model;

/**
 * Holds computed analytics for a single habit.
 */
public class HabitStats {
    private final int    currentStreak;
    private final int    longestStreak;
    private final double completionRate;
    private final int    totalCompletions;

    public HabitStats(int currentStreak, int longestStreak,
                      double completionRate, int totalCompletions) {
        this.currentStreak   = currentStreak;
        this.longestStreak   = longestStreak;
        this.completionRate  = completionRate;
        this.totalCompletions = totalCompletions;
    }

    public int    getCurrentStreak()   { return currentStreak; }
    public int    getLongestStreak()   { return longestStreak; }
    public double getCompletionRate()  { return completionRate; }
    public int    getTotalCompletions(){ return totalCompletions; }
}
