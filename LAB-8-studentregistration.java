Program : 

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("----------------------------------------------------");
        System.out.println("               STUDENT REGISTRATION FORM");
        System.out.println("----------------------------------------------------");

        System.out.print("USN            : ");
        String usn = sc.nextLine().trim();

        System.out.print("Name           : ");
        String name = sc.nextLine().trim();

        if (usn.isEmpty() || name.isEmpty()) {
            System.out.println("\nValidation Error: USN and Name are mandatory fields!");
            return;
        }

        System.out.println("Branch options : 1) Computer Science  2) Electronics  3) Mechanical  4) Civil");
        System.out.print("Choose branch (1-4): ");
        int branchChoice = Integer.parseInt(sc.nextLine().trim());
        String branch;
        if (branchChoice == 1) branch = "Computer Science";
        else if (branchChoice == 2) branch = "Electronics";
        else if (branchChoice == 3) branch = "Mechanical";
        else branch = "Civil";

        System.out.print("Gender (M/F)   : ");
        String genderInput = sc.nextLine().trim().toUpperCase();
        String gender;
        if (genderInput.equals("M")) gender = "Male";
        else if (genderInput.equals("F")) gender = "Female";
        else gender = "Not Selected";

        System.out.print("Skills - Java? (y/n): ");
        boolean javaSkill = sc.nextLine().trim().equalsIgnoreCase("y");
        System.out.print("Skills - Python? (y/n): ");
        boolean pythonSkill = sc.nextLine().trim().equalsIgnoreCase("y");

        String skills = "";
        if (javaSkill) skills += "Java ";
        if (pythonSkill) skills += "Python ";
        if (skills.isEmpty()) skills = "None";

        System.out.println();
        System.out.println("----------------------------------------------------");
        System.out.println("Student Details");
        System.out.println("----------------------------------------------------");
        System.out.println("USN     : " + usn);
        System.out.println("Name    : " + name);
        System.out.println("Branch  : " + branch);
        System.out.println("Gender  : " + gender);
        System.out.println("Skills  : " + skills.trim());
        System.out.println("----------------------------------------------------");
    }
}
