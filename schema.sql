-- ============================================================
--  Habit Tracker & Personal Analytics — Full Database Schema
--  Run this script once in MySQL to set up everything from scratch.
--  Compatible with MySQL 8.0+
-- ============================================================

-- 1. Create & select database
CREATE DATABASE IF NOT EXISTS habit_tracker_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE habit_tracker_db;

-- ============================================================
--  2. Users table
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(80)  NOT NULL,
    password_hash VARCHAR(64)  NOT NULL,   -- SHA-256 hex (64 chars)
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id),
    UNIQUE KEY uq_username (username)
) ENGINE=InnoDB;

-- ============================================================
--  3. Habits table
-- ============================================================
CREATE TABLE IF NOT EXISTS habits (
    habit_id         INT          NOT NULL AUTO_INCREMENT,
    user_id          INT          NOT NULL,
    habit_name       VARCHAR(120) NOT NULL,
    habit_type       ENUM('SIMPLE','QUANTIFIABLE') NOT NULL DEFAULT 'SIMPLE',
    goal_quantity    INT          NOT NULL DEFAULT 1,
    goal_units       VARCHAR(60)  NULL,          -- e.g. "glasses", "km"
    frequency        ENUM('DAILY','WEEKLY')       NOT NULL DEFAULT 'DAILY',
    frequency_target INT          NOT NULL DEFAULT 1,
    reminder_enabled TINYINT(1)   NOT NULL DEFAULT 0,
    reminder_time    VARCHAR(5)   NULL,          -- "HH:mm" e.g. "14:03"
    is_archived      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (habit_id),
    KEY idx_habits_user (user_id),
    CONSTRAINT fk_habits_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
--  4. Completion logs table
-- ============================================================
CREATE TABLE IF NOT EXISTS completion_logs (
    log_id          INT     NOT NULL AUTO_INCREMENT,
    habit_id        INT     NOT NULL,
    completion_date DATE    NOT NULL,
    logged_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (log_id),
    -- Prevents double-logging the same habit on the same day
    UNIQUE KEY uq_one_log_per_day (habit_id, completion_date),
    KEY idx_logs_date (completion_date),
    CONSTRAINT fk_logs_habit
        FOREIGN KEY (habit_id) REFERENCES habits (habit_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
--  5. Optional: seed a demo user (password = "demo")
--     SHA2('demo', 256) = 2a969c6a...
-- ============================================================
INSERT IGNORE INTO users (username, password_hash)
VALUES ('demo', SHA2('demo', 256));

-- ============================================================
--  Done.
-- ============================================================
