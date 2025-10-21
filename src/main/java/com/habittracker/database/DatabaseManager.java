package com.habittracker.database;

import com.habittracker.model.Habit;
import com.habittracker.model.User;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
private static final String DB_URL = "jdbc:mysql://localhost:3306/habit_tracker_db?useSSL=false";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public User validateUser(String username, String password) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Habit> getHabitsForUser(int userId) {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT habit_id, habit_name FROM habits WHERE user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                habits.add(new Habit(rs.getInt("habit_id"), rs.getString("habit_name"), userId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    public boolean addHabit(Habit habit) {
        String sql = "INSERT INTO habits (user_id, habit_name) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habit.getUserId());
            pstmt.setString(2, habit.getHabitName());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateHabit(Habit habit) {
        String sql = "UPDATE habits SET habit_name = ? WHERE habit_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, habit.getHabitName());
            pstmt.setInt(2, habit.getHabitId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteHabit(int habitId) {
        String sql = "DELETE FROM habits WHERE habit_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markHabitComplete(int habitId, LocalDate date) {
        String sql = "INSERT IGNORE INTO completion_logs (habit_id, completion_date) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setDate(2, Date.valueOf(date));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<LocalDate, Integer> getCompletionDataForChart(int userId, int days) {
        Map<LocalDate, Integer> completionData = new HashMap<>();
        String sql = "SELECT cl.completion_date, COUNT(cl.habit_id) as count " +
                     "FROM completion_logs cl JOIN habits h ON cl.habit_id = h.habit_id " +
                     "WHERE h.user_id = ? AND cl.completion_date >= ? " +
                     "GROUP BY cl.completion_date ORDER BY cl.completion_date ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now().minusDays(days)));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                completionData.put(rs.getDate("completion_date").toLocalDate(), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return completionData;
    }
}
