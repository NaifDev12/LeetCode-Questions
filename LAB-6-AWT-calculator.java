Program :

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("-----------------------------------");
        System.out.println("            Simple Calculator");
        System.out.println("-----------------------------------");

        System.out.print("First Number : ");
        double num1 = sc.nextDouble();

        System.out.print("Second Number : ");
        double num2 = sc.nextDouble();

        System.out.print("Click : ");
        String operation = sc.next();

        double result = 0;
        boolean valid = true;

        try {
            switch (operation) {
                case "Add":
                    result = num1 + num2;
                    break;
                case "Sub":
                    result = num1 - num2;
                    break;
                case "Mul":
                    result = num1 * num2;
                    break;
                case "Divide":
                    if (num2 == 0) {
                        System.out.println("Result : Cannot divide by zero");
                        valid = false;
                    } else {
                        result = num1 / num2;
                    }
                    break;
                default:
                    System.out.println("Result : Invalid operation");
                    valid = false;
            }
        } catch (Exception e) {
            System.out.println("Result : Invalid input");
            valid = false;
        }

        if (valid) {
            System.out.println("Result : " + result);
        }

        sc.close();
    }
}
