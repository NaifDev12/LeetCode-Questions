Program :
import java.util.Scanner;

public class PiCalculator {

    // Private variable - accessible only within this class
    private double piValue;
    private int terms;

    // Public constant - accessible from anywhere
    public static final String SERIES_NAME = "Leibniz Series (4/1 - 4/3 + 4/5 - 4/7 + 4/9 ...)";

    // Constructor - initializes private variables
    public PiCalculator(int terms) {
        this.terms = terms;
        this.piValue = computePi(terms);
    }

    // Private method - core calculation logic hidden from outside
    private double computePi(int terms) {
        double pi = 0.0;
        int sign = 1;

        for (int i = 0; i < terms; i++) {
            pi += sign * (4.0 / (2 * i + 1));
            sign *= -1;
        }
        return pi;
    }

    // Public method - accessible from anywhere, including main
    public void displayResult() {
        System.out.println("Public Method - Displaying Result:");
        System.out.println("Approximated value of Pi: " + piValue);
        System.out.println();
    }

    // Protected method - accessible within same package/subclasses
    protected void displayPrecisionInfo() {
        System.out.println("Protected Method - Displaying Precision Info:");
        System.out.println("Precision used: " + terms + " terms");
        System.out.println("Series used: " + SERIES_NAME);
        System.out.println();
    }

    // Public method demonstrating private data (accessed only within the class)
    public void showPrivateDataAccess() {
        System.out.println("Private Data - Accessed only within class:");
        System.out.println("Raw computed value (private): " + piValue);
    }

    // Public getter - safe way to retrieve Pi value from outside
    public double getPiValue() {
        return piValue;
    }

    // main method inside PiCalculator itself
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Pi Calculator using Access Specifiers ===");
        System.out.print("Enter the number of terms for Pi approximation: ");
        int terms = sc.nextInt();

        System.out.println("\nCalculating Pi using Leibniz Series...\n");

        // a. Create an object of PiCalculator
        PiCalculator calc = new PiCalculator(terms);

        // b. Access the public method to display the value of Pi
        calc.displayResult();

        // d. Call the protected method
        calc.displayPrecisionInfo();

        // Public method showing access to private data from within the class
        calc.showPrivateDataAccess();

        // c. Try accessing the private variable directly - NOT allowed
        // System.out.println(calc.piValue);
        // ERROR: piValue has private access in PiCalculator
        // Uncommenting the above line causes a compile-time error,
        // proving private members cannot be accessed from outside the class.

        sc.close();
    }
}
