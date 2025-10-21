# Habit Tracker & Personal Analytics

## Project Overview

The Habit Tracker & Personal Analytics is a desktop application designed to help users cultivate and monitor their personal habits. Built using Java Swing for a rich graphical user interface, this application allows users to create, track, and analyze their daily routines. Data persistence is managed securely using an offline MySQL database, ensuring that all user information and habit progress are stored locally and reliably. The project adheres to the Model-View-Controller (MVC) architectural pattern for modularity, maintainability, and clear separation of concerns.

## Features

* **User Authentication:** Secure login and registration for individual user accounts.
* **Habit Management:**
    * **Create Habits:** Define new habits with custom names.
    * **Edit Habits:** Modify existing habit details.
    * **Delete Habits:** Remove habits (with confirmation).
* **Habit Tracking:** Mark habits as complete for the current day.
* **Personal Analytics Dashboard:**
    * Visualize weekly progress (e.g., habits completed in the last 7 days).
    * Track consistency and completion streaks (implicitly through progress visualization).
* **Offline Data Persistence:** All user and habit data is stored in a local MySQL database.
* **Responsive Java Swing GUI:** An intuitive and user-friendly desktop interface.

## Technologies Used

* **Programming Language:** Java 17+
* **GUI Framework:** Java Swing
* **Database:** MySQL
* **Database Connectivity:** JDBC (Java Database Connectivity)
* **Build Tool:** Maven (recommended for dependency management)

## Architecture

The application follows the **Model-View-Controller (MVC)** architectural pattern:

* **Model:** Contains the core business logic (e.g., `Habit`, `User`, `CompletionLog` classes) and handles all interactions with the database.
* **View:** Implemented using Java Swing, responsible for the user interface and displaying data.
* **Controller:** Manages user input, updates the Model, and refreshes the View accordingly.

## Setup and Installation

### Prerequisites

1.  **Java Development Kit (JDK):** Version 17 or higher.
2.  **Maven:** For building the project and managing dependencies.
3.  **MySQL Server:** A locally installed MySQL server instance (e.g., XAMPP, WAMP, Docker, or standalone MySQL Community Server).
4.  **MySQL Workbench (Optional):** For easy database management and schema creation.

### Database Setup

1.  **Create Database:**
    Open your MySQL client (e.g., MySQL Workbench, command line) and execute the following SQL command to create the database:
    ```sql
    CREATE DATABASE IF NOT EXISTS habit_tracker_db;
    USE habit_tracker_db;
    ```
2.  **Database Schema:**
    The application will automatically generate the necessary tables (e.g., `users`, `habits`, `completion_logs`) upon first run due to Hibernate's `ddl-auto` setting, or you can create them manually using the DDL provided in the `src/main/resources/schema.sql` (if you choose to add one later).
    * **Note:** Ensure your MySQL user has appropriate permissions to create/modify tables in `habit_tracker_db`.

### Project Setup

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/your-username/habit-tracker.git](https://github.com/your-username/habit-tracker.git)
    cd habit-tracker
    ```
2.  **Configure Database Connection:**
    * Navigate to `src/main/resources/application.properties` (or `application.yml`).
    * Update the following properties with your local MySQL credentials:
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/habit_tracker_db?useSSL=false&serverTimezone=UTC
        spring.datasource.username=your_mysql_user
        spring.datasource.password=your_mysql_password
        spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
        ```
    * Replace `your_mysql_user` and `your_mysql_password` with your actual MySQL credentials.

3.  **Build the Project:**
    Open your terminal in the project root directory and run:
    ```bash
    mvn clean install
    ```
    This will download dependencies and compile the project.

4.  **Run the Application:**
    You can run the application directly from your IDE (e.g., IntelliJ IDEA, Eclipse) by running the `main` method in `HabitTrackerApplication.java`, or via Maven:
    ```bash
    mvn spring-boot:run
    ```
    The desktop GUI should launch.

## How to Use the Application

1.  **Login / Register:**
    * Upon launching, you'll see a login screen.
    * Enter your desired username and password and click "Register" to create a new account, or "Login" if you're an existing user.
2.  **Dashboard:**
    * After logging in, the main dashboard will display.
    * On the left, you'll see a list of your habits.
    * On the right, a "Weekly Progress" chart visualizes your completion history.
3.  **Manage Habits:**
    * **Add:** Click "Add", enter a habit name in the dialog, and click "OK".
    * **Edit:** Select a habit from the list, click "Edit", modify the name in the dialog, and click "OK".
    * **Delete:** Select a habit from the list, click "Delete". A confirmation dialog will appear. Click "Yes" to confirm deletion.
    * **Mark Today as Complete:** Select a habit you've completed and click "Mark Today as Complete" to log its completion.

## Future Enhancements

* **Cloud Synchronization:** Implement functionality to sync habit data to a cloud service, allowing multi-device access.
* **Advanced Analytics:** Introduce more sophisticated metrics, monthly views, long-term streak tracking, and customizable goal setting.
* **User-Defined Habit Types:** Support different types of habits (e.g., quantifiable "drink X glasses of water" vs. binary "read book").
* **Gamification:** Add elements like badges, points, or leaderboards to incentivize consistency.
* **Notification Reminders:** Integrate desktop notifications to proactively remind users about their habits.

## Contributing

(If this were an open-source project, you'd include guidelines here. For a class project, you might omit or mention for future reference.)

## License

(Specify your project's license, e.g., MIT, Apache 2.0, or "Proprietary for Academic Use".)