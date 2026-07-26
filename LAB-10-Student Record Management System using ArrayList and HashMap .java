
 
import java.util.*;

// ---------- Student Model ----------
class Student {
    private int id;
    private String name;
    private int age;
    private String course;
    private double marks;

    public Student(int id, String name, int age, String course, double marks) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.course = course;
        this.marks = marks;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public double getMarks() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }

    @Override
    public String toString() {
        return String.format("ID: %-5d Name: %-15s Age: %-4d Course: %-10s Marks: %.2f",
                id, name, age, course, marks);
    }
}

// ---------- Management System ----------
class StudentRecordManagementSystem {
    private ArrayList<Student> studentList;        // ordered storage
    private HashMap<Integer, Integer> idIndexMap;  // id -> index in ArrayList (fast lookup)

    public StudentRecordManagementSystem() {
        studentList = new ArrayList<>();
        idIndexMap = new HashMap<>();
    }

    // ---------- ADD ----------
    public void addStudent(int id, String name, int age, String course, double marks) {
        if (idIndexMap.containsKey(id)) {
            System.out.println("Error: Student with ID " + id + " already exists.");
            return;
        }
        Student student = new Student(id, name, age, course, marks);
        studentList.add(student);
        idIndexMap.put(id, studentList.size() - 1);
        System.out.println("Student added successfully: " + student);
    }

    // ---------- SEARCH ----------
    public Student searchStudent(int id) {
        Integer index = idIndexMap.get(id);
        if (index == null) {
            System.out.println("Student with ID " + id + " not found.");
            return null;
        }
        Student student = studentList.get(index);
        System.out.println("Student found: " + student);
        return student;
    }

    // ---------- UPDATE ----------
    public void updateStudent(int id, String name, int age, String course, double marks) {
        Integer index = idIndexMap.get(id);
        if (index == null) {
            System.out.println("Cannot update. Student with ID " + id + " not found.");
            return;
        }
        Student student = studentList.get(index);
        if (name != null && !name.isEmpty()) student.setName(name);
        if (age > 0) student.setAge(age);
        if (course != null && !course.isEmpty()) student.setCourse(course);
        if (marks >= 0) student.setMarks(marks);
        System.out.println("Student updated successfully: " + student);
    }

    // ---------- DELETE ----------
    public void deleteStudent(int id) {
        Integer index = idIndexMap.get(id);
        if (index == null) {
            System.out.println("Cannot delete. Student with ID " + id + " not found.");
            return;
        }

        int lastIndex = studentList.size() - 1;
        Student lastStudent = studentList.get(lastIndex);

        // Swap the student to remove with the last student to allow O(1) removal
        Collections.swap(studentList, index, lastIndex);
        idIndexMap.put(lastStudent.getId(), index);

        studentList.remove(lastIndex);
        idIndexMap.remove(id);

        System.out.println("Student with ID " + id + " deleted successfully.");
    }

    // ---------- DISPLAY ----------
    public void displayAllStudents() {
        if (studentList.isEmpty()) {
            System.out.println("No student records available.");
            return;
        }
        System.out.println("\n===== All Student Records =====");
        for (Student s : studentList) {
            System.out.println(s);
        }
        System.out.println("Total Students: " + studentList.size());
    }

    public int getTotalStudents() {
        return studentList.size();
    }
}

// ---------- Main / Menu-driven Program ----------
public class Main {
    public static void main(String[] args) {
        StudentRecordManagementSystem system = new StudentRecordManagementSystem();
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n===== Student Record Management System =====");
            System.out.println("1. Add Student");
            System.out.println("2. Update Student");
            System.out.println("3. Search Student");
            System.out.println("4. Delete Student");
            System.out.println("5. Display All Students");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            while (!sc.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                sc.next();
            }
            choice = sc.nextInt();

            switch (choice) {
                case 1: {
                    System.out.print("Enter ID: ");
                    int id = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Age: ");
                    int age = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Course: ");
                    String course = sc.nextLine();
                    System.out.print("Enter Marks: ");
                    double marks = sc.nextDouble();
                    system.addStudent(id, name, age, course, marks);
                    break;
                }
                case 2: {
                    System.out.print("Enter ID to update: ");
                    int id = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter new Name (leave blank to skip): ");
                    String name = sc.nextLine();
                    System.out.print("Enter new Age (0 to skip): ");
                    int age = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter new Course (leave blank to skip): ");
                    String course = sc.nextLine();
                    System.out.print("Enter new Marks (-1 to skip): ");
                    double marks = sc.nextDouble();
                    system.updateStudent(id, name, age, course, marks);
                    break;
                }
                case 3: {
                    System.out.print("Enter ID to search: ");
                    int id = sc.nextInt();
                    system.searchStudent(id);
                    break;
                }
                case 4: {
                    System.out.print("Enter ID to delete: ");
                    int id = sc.nextInt();
                    system.deleteStudent(id);
                    break;
                }
                case 5:
                    system.displayAllStudents();
                    break;
                case 6:
                    System.out.println("Exiting... Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        } while (choice != 6);

        sc.close();
    }
}
