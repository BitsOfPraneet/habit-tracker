# Habit Tracker & Personal Analytics

## Project Overview

The Habit Tracker & Personal Analytics is a comprehensive desktop application designed to help users cultivate, monitor, and analyze their personal habits. Built using Java Swing with a modern FlatLaf dark theme, this application offers a rich, responsive graphical user interface. 

Data persistence is securely managed using an offline MySQL database, ensuring that all user information and habit progress are stored locally and reliably. The project adheres to the Model-View-Controller (MVC) architectural pattern and utilizes multi-threading (`SwingWorker`) to ensure the user interface remains fluid and non-blocking during database operations.

## Features

* **User Authentication:** Secure login and registration with SHA-256 password hashing.
* **Advanced Habit Management:**
    * **Create/Edit Habits:** Define habits with custom names, types (Simple binary or Quantifiable with specific targets and units), and frequencies (Daily or Weekly).
    * **Archiving:** Archive older habits to keep your active list clean without losing historical data. Easily unarchive them later from a collapsible section.
    * **Delete Habits:** Completely remove habits and their logs (with confirmation safeguards).
* **Proactive Engagement:**
    * **Custom Reminders:** Set specific times for individual habits to trigger system tray notifications.
* **Personal Analytics Dashboard:**
    * **Weekly Progress Chart:** A dynamic bar chart (powered by JFreeChart) displaying the number of completions over the last 7 days.
    * **Annual Heatmap:** A full-year, GitHub-style completion heatmap to visualize long-term consistency.
    * **Metrics:** Automatically calculates Current Streak, Longest Streak, and overall Completion Rate.
* **Modern UI & Performance:**
    * Beautiful dark mode aesthetic using the FlatLaf library.
    * Asynchronous database operations guarantee a smooth, stutter-free experience.

## Technologies Used

* **Programming Language:** Java 11+
* **GUI Framework:** Java Swing
* **Look and Feel:** FlatLaf (Dark Theme)
* **Charting:** JFreeChart
* **Database:** MySQL 8.0+
* **Database Connectivity:** JDBC (Java Database Connectivity)
* **Build Tool:** Maven

## Setup and Installation

### Prerequisites

1.  **Java Development Kit (JDK):** Version 11 or higher.
2.  **Maven:** For building the project and managing dependencies.
3.  **MySQL Server:** A locally installed MySQL server instance.

### Database Setup

1.  **Initialize the Database:**
    The project includes a `schema.sql` file that sets up the database, tables, and necessary relationships. Run this script in your MySQL environment. 
    
    Using PowerShell/Command Prompt:
    ```bash
    Get-Content schema.sql | mysql -u root -p
    ```
    *(You will be prompted for your MySQL root password).*

    Alternatively, you can open `schema.sql` and run its contents directly inside MySQL Workbench.

2.  **Database Configuration:**
    The database connection details are located in `src/main/java/com/habittracker/database/DatabaseManager.java`. By default, it connects to:
    * **URL:** `jdbc:mysql://localhost:3306/habit_tracker_db`
    * **User:** `root`
    * **Password:** `root`
    *(Modify these credentials in `DatabaseManager.java` if your local MySQL setup differs).*

3.  **Optional - Load Seed Data:**
    If you want to populate the application with sample habits and completion data to see the analytics in action, run the `seed_data.sql` file (ensure you have registered a user named `kazama` first, or modify the script to match your username):
    ```bash
    Get-Content seed_data.sql | mysql -u root -p
    ```

### Running the Application

1.  **Build and Run via Maven:**
    Open your terminal in the project root directory and run the following command to clean, compile, and execute the application:
    
    **Windows (PowerShell):**
    ```powershell
    mvn clean compile exec:java "-Dexec.mainClass=com.habittracker.Main"
    ```
    
    **Mac/Linux:**
    ```bash
    mvn clean compile exec:java -Dexec.mainClass="com.habittracker.Main"
    ```

## How to Use the Application

1.  **Login / Register:**
    * Upon launching, enter your desired credentials. Click "Register" for a new account, or "Login" if returning.
2.  **Dashboard:**
    * Your active habits are listed on the left.
    * The center panel displays detailed analytics (streaks and heatmap) for the currently selected habit.
    * The right panel shows your overall weekly progress chart.
3.  **Manage Habits:**
    * **Add:** Click "Add" to define a new habit, set its frequency, goals, and optional reminder times.
    * **Log Completion:** Select an active habit and click "Log Completion" to record your success for today.
    * **Archive/Unarchive:** Edit a habit to check "Archive this habit". It will move to the collapsible "Archived Habits" section at the bottom left, where it can be selected and unarchived.
    * **Delete:** Select a habit and click "Delete" to permanently remove it and all associated data.
