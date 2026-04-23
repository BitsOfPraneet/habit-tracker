package com.habittracker.model;

/**
 * Represents a Habit entity with all properties as defined in the project spec.
 */
public class Habit {
    private int habitId;
    private int userId;
    private String habitName;
    /** "SIMPLE" or "QUANTIFIABLE" */
    private String habitType;
    private int goalQuantity;
    private String goalUnits;
    /** "DAILY" or "WEEKLY" */
    private String frequency;
    private int frequencyTarget;
    private boolean reminderEnabled;
    /** Stored as "HH:mm" string, e.g. "14:03" */
    private String reminderTime;
    private boolean archived;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    /** Full constructor used when loading from DB. */
    public Habit(int habitId, int userId, String habitName, String habitType,
                 int goalQuantity, String goalUnits, String frequency,
                 int frequencyTarget, boolean reminderEnabled,
                 String reminderTime, boolean archived) {
        this.habitId        = habitId;
        this.userId         = userId;
        this.habitName      = habitName;
        this.habitType      = habitType;
        this.goalQuantity   = goalQuantity;
        this.goalUnits      = goalUnits;
        this.frequency      = frequency;
        this.frequencyTarget = frequencyTarget;
        this.reminderEnabled = reminderEnabled;
        this.reminderTime   = reminderTime;
        this.archived       = archived;
    }

    /** Minimal constructor for new habits (habitId assigned by DB). */
    public Habit(int userId, String habitName, String habitType,
                 int goalQuantity, String goalUnits, String frequency,
                 int frequencyTarget, boolean reminderEnabled,
                 String reminderTime, boolean archived) {
        this(-1, userId, habitName, habitType, goalQuantity, goalUnits,
             frequency, frequencyTarget, reminderEnabled, reminderTime, archived);
    }

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------

    public int    getHabitId()         { return habitId; }
    public void   setHabitId(int id)   { this.habitId = id; }

    public int    getUserId()          { return userId; }

    public String getHabitName()       { return habitName; }
    public void   setHabitName(String n) { this.habitName = n; }

    public String getHabitType()       { return habitType; }
    public void   setHabitType(String t) { this.habitType = t; }

    public int    getGoalQuantity()    { return goalQuantity; }
    public void   setGoalQuantity(int q) { this.goalQuantity = q; }

    public String getGoalUnits()       { return goalUnits; }
    public void   setGoalUnits(String u) { this.goalUnits = u; }

    public String getFrequency()       { return frequency; }
    public void   setFrequency(String f) { this.frequency = f; }

    public int    getFrequencyTarget() { return frequencyTarget; }
    public void   setFrequencyTarget(int t) { this.frequencyTarget = t; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void    setReminderEnabled(boolean b) { this.reminderEnabled = b; }

    public String getReminderTime()    { return reminderTime; }
    public void   setReminderTime(String t) { this.reminderTime = t; }

    public boolean isArchived()        { return archived; }
    public void    setArchived(boolean a) { this.archived = a; }

    @Override
    public String toString() { return habitName; }
}