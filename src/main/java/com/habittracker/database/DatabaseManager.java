package com.habittracker.database;

import com.habittracker.model.Habit;
import com.habittracker.model.HabitStats;
import com.habittracker.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Central data-access layer. Every public method opens and closes its own
 * connection so callers (SwingWorker threads) don't need to worry about sharing.
 */
public class DatabaseManager {

    // ---------------------------------------------------------------
    //  Change these to match your MySQL installation
    // ---------------------------------------------------------------
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/habit_tracker_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "root";

    // ---------------------------------------------------------------
    //  Connection helper
    // ---------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ---------------------------------------------------------------
    //  Auth
    // ---------------------------------------------------------------

    public User validateUser(String username, String password) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND password_hash = SHA2(?, 256)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new User(rs.getInt("user_id"), username);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, SHA2(?, 256))";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---------------------------------------------------------------
    //  Habits – CRUD
    // ---------------------------------------------------------------

    /** Returns active (non-archived) habits for the user. */
    public List<Habit> getHabitsForUser(int userId) {
        return getHabitsForUser(userId, false);
    }

    /** Returns habits; if includeArchived is true, archived ones are also returned. */
    public List<Habit> getHabitsForUser(int userId, boolean includeArchived) {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT * FROM habits WHERE user_id = ?"
                   + (includeArchived ? "" : " AND is_archived = FALSE")
                   + " ORDER BY habit_name";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) habits.add(mapHabit(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return habits;
    }

    public Habit getHabitById(int habitId) {
        String sql = "SELECT * FROM habits WHERE habit_id = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapHabit(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean addHabit(Habit h) {
        String sql = "INSERT INTO habits "
                   + "(user_id, habit_name, habit_type, goal_quantity, goal_units, "
                   + " frequency, frequency_target, reminder_enabled, reminder_time, is_archived) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillHabitParams(ps, h);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) h.setHabitId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateHabit(Habit h) {
        String sql = "UPDATE habits SET "
                   + "habit_name=?, habit_type=?, goal_quantity=?, goal_units=?, "
                   + "frequency=?, frequency_target=?, reminder_enabled=?, reminder_time=?, is_archived=? "
                   + "WHERE habit_id=?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, h.getHabitName());
            ps.setString(2, h.getHabitType());
            ps.setInt   (3, h.getGoalQuantity());
            ps.setString(4, h.getGoalUnits());
            ps.setString(5, h.getFrequency());
            ps.setInt   (6, h.getFrequencyTarget());
            ps.setBoolean(7, h.isReminderEnabled());
            ps.setString(8, h.getReminderTime());
            ps.setBoolean(9, h.isArchived());
            ps.setInt   (10, h.getHabitId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteHabit(int habitId) {
        // completion_logs are deleted via ON DELETE CASCADE in DB
        String sql = "DELETE FROM habits WHERE habit_id = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---------------------------------------------------------------
    //  Completion Logging
    // ---------------------------------------------------------------

    /** Returns true if a new row was inserted (first completion today). */
    public boolean markHabitComplete(int habitId, LocalDate date) {
        String sql = "INSERT IGNORE INTO completion_logs (habit_id, completion_date) VALUES (?, ?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Returns whether the habit is already logged today. */
    public boolean isCompletedToday(int habitId) {
        String sql = "SELECT COUNT(*) FROM completion_logs WHERE habit_id=? AND completion_date=?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---------------------------------------------------------------
    //  Analytics
    // ---------------------------------------------------------------

    /**
     * Returns completion counts per day for the last {@code days} days,
     * across all habits belonging to this user.
     */
    public Map<LocalDate, Integer> getCompletionDataForChart(int userId, int days) {
        Map<LocalDate, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT cl.completion_date, COUNT(cl.habit_id) AS cnt "
                   + "FROM completion_logs cl "
                   + "JOIN habits h ON cl.habit_id = h.habit_id "
                   + "WHERE h.user_id = ? AND cl.completion_date >= ? "
                   + "GROUP BY cl.completion_date ORDER BY cl.completion_date";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(days - 1)));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                data.put(rs.getDate("completion_date").toLocalDate(), rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * Returns all completion dates for a single habit (for heatmap coloring).
     */
    public Set<LocalDate> getCompletionDatesForHabit(int habitId) {
        Set<LocalDate> dates = new HashSet<>();
        String sql = "SELECT completion_date FROM completion_logs WHERE habit_id=?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                dates.add(rs.getDate("completion_date").toLocalDate());
        } catch (SQLException e) { e.printStackTrace(); }
        return dates;
    }

    /**
     * Computes current streak, longest streak, completion rate, and total
     * completions for a given habit.
     */
    public HabitStats getStatsForHabit(int habitId) {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT completion_date FROM completion_logs "
                   + "WHERE habit_id=? ORDER BY completion_date ASC";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, habitId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                dates.add(rs.getDate("completion_date").toLocalDate());
        } catch (SQLException e) { e.printStackTrace(); }

        int total = dates.size();
        if (total == 0) return new HabitStats(0, 0, 0.0, 0);

        // Compute streaks (daily habit assumption for simplicity)
        int currentStreak = 0, longestStreak = 0, tempStreak = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).minusDays(1).equals(dates.get(i - 1))) {
                tempStreak++;
            } else {
                longestStreak = Math.max(longestStreak, tempStreak);
                tempStreak = 1;
            }
        }
        longestStreak = Math.max(longestStreak, tempStreak);

        // Current streak: walk back from today
        LocalDate check = LocalDate.now();
        Set<LocalDate> dateSet = new HashSet<>(dates);
        while (dateSet.contains(check)) {
            currentStreak++;
            check = check.minusDays(1);
        }

        // Completion rate: total completions / days since first log
        LocalDate first = dates.get(0);
        long daysSinceFirst = LocalDate.now().toEpochDay() - first.toEpochDay() + 1;
        double rate = (daysSinceFirst > 0) ? (total * 100.0 / daysSinceFirst) : 0;
        rate = Math.min(rate, 100.0);

        return new HabitStats(currentStreak, longestStreak, rate, total);
    }

    // ---------------------------------------------------------------
    //  Reminder helpers
    // ---------------------------------------------------------------

    /** Returns all habits that have reminders enabled, for any user. */
    public List<Habit> getHabitsWithReminders(int userId) {
        List<Habit> result = new ArrayList<>();
        String sql = "SELECT * FROM habits WHERE user_id=? AND reminder_enabled=TRUE AND is_archived=FALSE";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapHabit(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // ---------------------------------------------------------------
    //  Private helpers
    // ---------------------------------------------------------------

    private Habit mapHabit(ResultSet rs) throws SQLException {
        return new Habit(
            rs.getInt    ("habit_id"),
            rs.getInt    ("user_id"),
            rs.getString ("habit_name"),
            rs.getString ("habit_type"),
            rs.getInt    ("goal_quantity"),
            rs.getString ("goal_units"),
            rs.getString ("frequency"),
            rs.getInt    ("frequency_target"),
            rs.getBoolean("reminder_enabled"),
            rs.getString ("reminder_time"),
            rs.getBoolean("is_archived")
        );
    }

    private void fillHabitParams(PreparedStatement ps, Habit h) throws SQLException {
        ps.setInt    (1, h.getUserId());
        ps.setString (2, h.getHabitName());
        ps.setString (3, h.getHabitType());
        ps.setInt    (4, h.getGoalQuantity());
        ps.setString (5, h.getGoalUnits());
        ps.setString (6, h.getFrequency());
        ps.setInt    (7, h.getFrequencyTarget());
        ps.setBoolean(8, h.isReminderEnabled());
        ps.setString (9, h.getReminderTime());
        ps.setBoolean(10, h.isArchived());
    }
}
