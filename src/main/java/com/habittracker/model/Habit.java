package com.habittracker.model;


public class Habit {
    private int habitId;
    private final String habitName;
    private final int userId;

    public Habit(int habitId, String habitName, int userId) {
        this.habitId = habitId;
        this.habitName = habitName;
        this.userId = userId;
    }

    public Habit(String habitName, int userId) {
        this.habitName = habitName;
        this.userId = userId;
    }

    public int getHabitId() { return habitId; }
    public String getHabitName() { return habitName; }
    public int getUserId() { return userId; }
}