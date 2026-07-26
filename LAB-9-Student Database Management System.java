Program :

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * Centralized JDBC connection utility.
 * Update the URL, USER, and PASSWORD to match your local MySQL setup.
 */
public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/student_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password_here";

    // Prevent instantiation
    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Not strictly required with modern JDBC 4+ drivers (auto-loaded via SPI),
            // but kept for clarity / compatibility with older setups.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j to your classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}



README

# Student Database Management System (Java + JDBC)

A console-based CRUD application for managing student records, backed by MySQL.

## Project structure
```
StudentDBMS/
├── schema.sql              -- run this in MySQL first
├── src/
│   ├── Student.java         -- model class
│   ├── DBConnection.java    -- JDBC connection helper
│   ├── StudentDAO.java      -- CRUD logic (Create, Read, Update, Delete)
│   └── StudentDBMSApp.java  -- console menu / main class
```

## 1. Set up the database
Open MySQL and run:
```
mysql -u root -p < schema.sql
```
or paste the contents of `schema.sql` into your MySQL client / Workbench.

## 2. Get the JDBC driver
Download **MySQL Connector/J** (the `mysql-connector-j-<version>.jar` file) from:
https://dev.mysql.com/downloads/connector/j/

## 3. Configure credentials
Edit `src/DBConnection.java` and update:
```java
private static final String URL  = "jdbc:mysql://localhost:3306/student_db?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "your_password_here";
```

## 4. Compile
```
cd StudentDBMS
javac -d bin src/*.java
```

## 5. Run
Include the connector jar on the classpath:

**Linux / macOS**
```
java -cp "bin:mysql-connector-j-9.x.x.jar" StudentDBMSApp
```

**Windows**
```
java -cp "bin;mysql-connector-j-9.x.x.jar" StudentDBMSApp
```

## Features (CRUD)
| Operation | Menu option | Description |
|---|---|---|
| Create | 1 | Add a new student record |
| Read   | 2 | View all students |
| Read   | 3 | View a single student by ID |
| Read   | 4 | Search students by (partial) name |
| Update | 5 | Update an existing student's details |
| Delete | 6 | Delete a student by ID |

## Notes
- Uses `PreparedStatement` everywhere to prevent SQL injection.
- `DBConnection` opens a fresh connection per operation and each DAO method
  closes it via try-with-resources — simple and safe for a learning project.
  For a production app, you'd typically use a connection pool (e.g., HikariCP).
- Feel free to extend: add pagination, sorting, export-to-CSV, or a Swing/JavaFX GUI on top of the same `StudentDAO`.



SCHEMA

-- ============================================
-- Student Database Management System
-- Database schema setup script
-- Run this in MySQL before running the Java app
-- ============================================

CREATE DATABASE IF NOT EXISTS student_db;
USE student_db;

CREATE TABLE IF NOT EXISTS students (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    age         INT NOT NULL,
    course      VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE,
    marks       DOUBLE
);

-- Optional sample data
INSERT INTO students (name, age, course, email, marks) VALUES
('Aditi Rao', 20, 'Computer Science', 'aditi.rao@example.com', 88.5),
('Rohan Mehta', 21, 'Electronics', 'rohan.mehta@example.com', 75.0);


STUDENT

/**
 * Student.java
 * Model class representing a single student record.
 * Maps directly to a row in the "students" table.
 */
public class Student {

    private int id;
    private String name;
    private int age;
    private String course;
    private String email;
    private double marks;

    public Student() {
    }

    public Student(String name, int age, String course, String email, double marks) {
        this.name = name;
        this.age = age;
        this.course = course;
        this.email = email;
        this.marks = marks;
    }

    public Student(int id, String name, int age, String course, String email, double marks) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.course = course;
        this.email = email;
        this.marks = marks;
    }

    // ---------- Getters & Setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %-4d | Name: %-20s | Age: %-3d | Course: %-20s | Email: %-25s | Marks: %.2f",
                id, name, age, course, email, marks
        );
    }
}




StudentDAO

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentDAO.java
 * Data Access Object encapsulating all CRUD (Create, Read, Update, Delete)
 * operations for the "students" table. Uses PreparedStatement throughout
 * to prevent SQL injection.
 */
public class StudentDAO {

    // ---------------- CREATE ----------------
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (name, age, course, email, marks) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getName());
            ps.setInt(2, student.getAge());
            ps.setString(3, student.getCourse());
            ps.setString(4, student.getEmail());
            ps.setDouble(5, student.getMarks());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        student.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }

    // ---------------- READ (all) ----------------
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching students: " + e.getMessage());
        }
        return students;
    }

    // ---------------- READ (by id) ----------------
    public Student getStudentById(int id) {
        String sql = "SELECT * FROM students WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching student: " + e.getMessage());
        }
        return null; // not found
    }

    // ---------------- READ (search by name, partial match) ----------------
    public List<Student> searchByName(String name) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE name LIKE ? ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error searching students: " + e.getMessage());
        }
        return students;
    }

    // ---------------- UPDATE ----------------
    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET name = ?, age = ?, course = ?, email = ?, marks = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getName());
            ps.setInt(2, student.getAge());
            ps.setString(3, student.getCourse());
            ps.setString(4, student.getEmail());
            ps.setDouble(5, student.getMarks());
            ps.setInt(6, student.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    // ---------------- DELETE ----------------
    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }

    // ---------------- Helper: map a ResultSet row to a Student object ----------------
    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("course"),
                rs.getString("email"),
                rs.getDouble("marks")
        );
    }
}


StudentDBMS APP

import java.util.List;
import java.util.Scanner;

/**
 * StudentDBMSApp.java
 * Console front-end for the Student Database Management System.
 * Presents a menu and delegates all data operations to StudentDAO.
 */
public class StudentDBMSApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentDAO studentDAO = new StudentDAO();

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("=========================================");
        System.out.println(" STUDENT DATABASE MANAGEMENT SYSTEM (JDBC)");
        System.out.println("=========================================");

        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewAllStudents();
                case 3 -> viewStudentById();
                case 4 -> searchStudentsByName();
                case 5 -> updateStudent();
                case 6 -> deleteStudent();
                case 0 -> {
                    running = false;
                    System.out.println("Exiting. Goodbye!");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
            System.out.println();
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("---------------------------------------");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. View Student by ID");
        System.out.println("4. Search Students by Name");
        System.out.println("5. Update Student");
        System.out.println("6. Delete Student");
        System.out.println("0. Exit");
        System.out.println("---------------------------------------");
    }

    // ---------------- Menu actions ----------------

    private static void addStudent() {
        System.out.println("\n-- Add New Student --");
        String name = readString("Name: ");
        int age = readInt("Age: ");
        String course = readString("Course: ");
        String email = readString("Email: ");
        double marks = readDouble("Marks: ");

        Student student = new Student(name, age, course, email, marks);
        boolean success = studentDAO.addStudent(student);

        if (success) {
            System.out.println("Student added successfully with ID: " + student.getId());
        } else {
            System.out.println("Failed to add student.");
        }
    }

    private static void viewAllStudents() {
        System.out.println("\n-- All Students --");
        List<Student> students = studentDAO.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No student records found.");
        } else {
            for (Student s : students) {
                System.out.println(s);
            }
        }
    }

    private static void viewStudentById() {
        System.out.println("\n-- View Student by ID --");
        int id = readInt("Enter student ID: ");
        Student student = studentDAO.getStudentById(id);

        if (student != null) {
            System.out.println(student);
        } else {
            System.out.println("No student found with ID: " + id);
        }
    }

    private static void searchStudentsByName() {
        System.out.println("\n-- Search Students by Name --");
        String name = readString("Enter name (or part of it): ");
        List<Student> results = studentDAO.searchByName(name);

        if (results.isEmpty()) {
            System.out.println("No matching students found.");
        } else {
            for (Student s : results) {
                System.out.println(s);
            }
        }
    }

    private static void updateStudent() {
        System.out.println("\n-- Update Student --");
        int id = readInt("Enter ID of student to update: ");
        Student existing = studentDAO.getStudentById(id);

        if (existing == null) {
            System.out.println("No student found with ID: " + id);
            return;
        }

        System.out.println("Current record: " + existing);
        System.out.println("Enter new values (leave blank to keep current value):");

        String name = readOptionalString("Name [" + existing.getName() + "]: ", existing.getName());
        int age = readOptionalInt("Age [" + existing.getAge() + "]: ", existing.getAge());
        String course = readOptionalString("Course [" + existing.getCourse() + "]: ", existing.getCourse());
        String email = readOptionalString("Email [" + existing.getEmail() + "]: ", existing.getEmail());
        double marks = readOptionalDouble("Marks [" + existing.getMarks() + "]: ", existing.getMarks());

        existing.setName(name);
        existing.setAge(age);
        existing.setCourse(course);
        existing.setEmail(email);
        existing.setMarks(marks);

        boolean success = studentDAO.updateStudent(existing);
        System.out.println(success ? "Student updated successfully." : "Failed to update student.");
    }

    private static void deleteStudent() {
        System.out.println("\n-- Delete Student --");
        int id = readInt("Enter ID of student to delete: ");

        Student existing = studentDAO.getStudentById(id);
        if (existing == null) {
            System.out.println("No student found with ID: " + id);
            return;
        }

        System.out.println("About to delete: " + existing);
        String confirm = readString("Are you sure? (y/n): ");

        if (confirm.equalsIgnoreCase("y")) {
            boolean success = studentDAO.deleteStudent(id);
            System.out.println(success ? "Student deleted successfully." : "Failed to delete student.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // ---------------- Input helpers (with basic validation) ----------------

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readOptionalString(String prompt, String currentValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentValue : input;
    }

    private static int readOptionalInt(String prompt, int currentValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return currentValue;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number, keeping current value.");
            return currentValue;
        }
    }

    private static double readOptionalDouble(String prompt, double currentValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return currentValue;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number, keeping current value.");
            return currentValue;
        }
    }
}








