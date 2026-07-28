import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Project: Secure Pass Guard (Password Manager & Security Strength Checker)
 * Aim: A Java-based cybersecurity application that securely manages passwords,
 *      checks password strength, generates strong passwords, and persistently stores data
 *      in a SQL Database or persistent local storage across application restarts.
 * 
 * NOTE: All SQL Database Schemas, Table DDLs, Cryptography, and Java logic are fully 
 *       self-contained in this SINGLE file.
 */
public class SecurePassGuard {

    private static final Scanner scanner = new Scanner(System.in);
    private static final String DATA_FILE = "securepassguard_data.dat";
    
    // -----------------------------------------------------------------
    // EMBEDDED SQL DATABASE DDL SCHEMAS
    // -----------------------------------------------------------------
    public static final String SQL_SCHEMA_USERS = 
        "CREATE TABLE IF NOT EXISTS users (\n" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
        "    username VARCHAR(50) UNIQUE NOT NULL,\n" +
        "    password_hash VARCHAR(256) NOT NULL,\n" +
        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
        ");";

    public static final String SQL_SCHEMA_CREDENTIALS = 
        "CREATE TABLE IF NOT EXISTS credentials (\n" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
        "    user_id INTEGER NOT NULL,\n" +
        "    website VARCHAR(100) NOT NULL,\n" +
        "    username VARCHAR(100) NOT NULL,\n" +
        "    encrypted_password VARCHAR(256) NOT NULL,\n" +
        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
        "    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
        ");";

    // JDBC Connection details
    private static final String DB_URL = "jdbc:sqlite:securepassguard.db";
    private static Connection conn = null;
    private static boolean useDatabase = false;

    // Current Logged-in User Session
    private static User currentUser = null;

    // Persistent Storage Map for fallback persistence across process restarts
    private static final Map<String, User> memoryUserDb = new HashMap<>();

    // AES Encryption Key setup (128-bit key derived via SHA-256)
    private static final String AES_SECRET = "SecurePassGuardSecretKey123!";
    private static SecretKeySpec secretKeySpec;

    static {
        try {
            byte[] key = AES_SECRET.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // 128-bit key
            secretKeySpec = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Model Classes
    static class User {
        int id;
        String username;
        String passwordHash;
        List<Credential> memoryCredentials = new ArrayList<>();

        User(int id, String username, String passwordHash) {
            this.id = id;
            this.username = username;
            this.passwordHash = passwordHash;
        }
    }

    static class Credential {
        int id;
        String website;
        String username;
        String encryptedPassword;

        Credential(int id, String website, String username, String encryptedPassword) {
            this.id = id;
            this.website = website;
            this.username = username;
            this.encryptedPassword = encryptedPassword;
        }
    }

    public static void main(String[] args) {
        // Initialize Storage (SQL Database or File Persistence)
        initStorage();

        System.out.println("==================================================");
        System.out.println("     SECURE PASS GUARD - PASSWORD MANAGER       ");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            displayMainMenu();
            System.out.print("Select Choice (1-7): ");
            if (!scanner.hasNextLine()) break;
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                case "Register":
                case "register":
                    registerUser();
                    break;
                case "2":
                case "Login":
                case "login":
                    loginUser();
                    break;
                case "3":
                case "Password Manager":
                case "password manager":
                    handlePasswordManager();
                    break;
                case "4":
                case "Password Strength Checker":
                case "password strength checker":
                    checkPasswordStrength();
                    break;
                case "5":
                case "Password Generator":
                case "password generator":
                    generatePassword();
                    break;
                case "6":
                case "Logout":
                case "logout":
                    logoutUser();
                    break;
                case "7":
                case "Exit":
                case "exit":
                    running = false;
                    closeDatabase();
                    System.out.println("\nExiting Secure Pass Guard. Security is a habit, keep your passwords safe!");
                    break;
                default:
                    System.out.println("Invalid option! Please enter a number between 1 and 7.");
            }
            System.out.println();
        }
    }

    // -----------------------------------------------------------------
    // Storage & Database Initialization (SQL + File Persistence)
    // -----------------------------------------------------------------
    private static void initStorage() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            useDatabase = true;

            Statement stmt = conn.createStatement();
            stmt.execute(SQL_SCHEMA_USERS);
            stmt.execute(SQL_SCHEMA_CREDENTIALS);
            stmt.close();
        } catch (Exception e) {
            // Fallback to File Persistence mode if JDBC driver is not on classpath
            useDatabase = false;
            loadDataFromFile();
        }
    }

    private static void closeDatabase() {
        if (useDatabase && conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {}
        } else if (!useDatabase) {
            saveDataToFile();
        }
    }

    private static void loadDataFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            User activeUser = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("USER:")) {
                    String[] parts = line.substring(5).split("::", 3);
                    if (parts.length >= 3) {
                        int id = Integer.parseInt(parts[0]);
                        String username = parts[1];
                        String hash = parts[2];
                        activeUser = new User(id, username, hash);
                        memoryUserDb.put(username.toLowerCase(), activeUser);
                    }
                } else if (line.startsWith("CRED:") && activeUser != null) {
                    String[] parts = line.substring(5).split("::", 4);
                    if (parts.length >= 4) {
                        int id = Integer.parseInt(parts[0]);
                        String website = parts[1];
                        String username = parts[2];
                        String encPass = parts[3];
                        activeUser.memoryCredentials.add(new Credential(id, website, username, encPass));
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void saveDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE, StandardCharsets.UTF_8))) {
            for (User u : memoryUserDb.values()) {
                writer.write("USER:" + u.id + "::" + u.username + "::" + u.passwordHash);
                writer.newLine();
                for (Credential c : u.memoryCredentials) {
                    writer.write("CRED:" + c.id + "::" + c.website + "::" + c.username + "::" + c.encryptedPassword);
                    writer.newLine();
                }
            }
        } catch (Exception ignored) {}
    }

    private static void displayMainMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        if (currentUser != null) {
            System.out.println("Logged in as: " + currentUser.username);
        } else {
            System.out.println("Status: Not Logged In");
        }
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Password Manager");
        System.out.println("4. Password Strength Checker");
        System.out.println("5. Password Generator");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
        System.out.println("-------------------------------------------");
    }

    // -----------------------------------------------------------------
    // 1. User Registration, Authentication & Logout
    // -----------------------------------------------------------------
    private static void registerUser() {
        System.out.println("\n--- USER REGISTRATION ---");
        System.out.print("Username: ");
        if (!scanner.hasNextLine()) return;
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        // Check if username is already registered
        User existing = findUserByUsername(username);
        if (existing != null) {
            System.out.println("Username '" + username + "' is already registered!");
            System.out.print("Would you like to log in as '" + username + "' now? (yes/no): ");
            if (scanner.hasNextLine() && scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                System.out.print("Enter Password: ");
                if (scanner.hasNextLine()) {
                    String pass = scanner.nextLine().trim();
                    if (existing.passwordHash.equals(hashPassword(pass))) {
                        currentUser = existing;
                        System.out.println("Login successful! Welcome back, " + currentUser.username + ".");
                        return;
                    } else {
                        System.out.println("Incorrect password. Returning to main menu.");
                        return;
                    }
                }
            }
            return;
        }

        System.out.print("Password: ");
        if (!scanner.hasNextLine()) return;
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        String passwordHash = hashPassword(password);

        if (useDatabase) {
            String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, passwordHash);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                int userId = 0;
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
                currentUser = new User(userId, username, passwordHash);
                System.out.println("User '" + username + "' registered and logged in successfully!");
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        } else {
            User newUser = new User(memoryUserDb.size() + 1, username, passwordHash);
            memoryUserDb.put(username.toLowerCase(), newUser);
            currentUser = newUser;
            saveDataToFile();
            System.out.println("User '" + username + "' registered and logged in successfully!");
        }
    }

    private static void loginUser() {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Username: ");
        if (!scanner.hasNextLine()) return;
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        if (!scanner.hasNextLine()) return;
        String password = scanner.nextLine().trim();

        User user = findUserByUsername(username);
        if (user != null && user.passwordHash.equals(hashPassword(password))) {
            currentUser = user;
            System.out.println("Login successful! Welcome, " + currentUser.username + ".");
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
    }

    private static void logoutUser() {
        if (currentUser == null) {
            System.out.println("No user is currently logged in.");
        } else {
            System.out.println("Logged out successfully. Goodbye, " + currentUser.username + "!");
            currentUser = null;
        }
    }

    private static User findUserByUsername(String username) {
        if (useDatabase) {
            String sql = "SELECT id, username, password_hash FROM users WHERE LOWER(username) = LOWER(?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"));
                }
            } catch (SQLException e) {
                System.out.println("Database query error: " + e.getMessage());
            }
            return null;
        } else {
            return memoryUserDb.get(username.toLowerCase());
        }
    }

    private static boolean ensureAuthenticated() {
        if (currentUser != null) {
            return true;
        }

        System.out.println("\nAuthentication required to access Password Manager.");
        System.out.print("Do you have an account? (yes/no): ");
        if (!scanner.hasNextLine()) return false;
        String ans = scanner.nextLine().trim().toLowerCase();

        if (ans.startsWith("y")) {
            loginUser();
        } else {
            registerUser();
        }

        return currentUser != null;
    }

    // -----------------------------------------------------------------
    // 2. Password Strength Checker
    // -----------------------------------------------------------------
    private static void checkPasswordStrength() {
        System.out.println("\n--- PASSWORD STRENGTH CHECKER ---");
        
        System.out.print("Username: ");
        if (!scanner.hasNextLine()) return;
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        if (!scanner.hasNextLine()) return;
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        int score = 0;
        List<String> suggestions = new ArrayList<>();

        if (password.length() >= 8) score++;
        else suggestions.add("Password length should be at least 8 characters.");

        if (password.matches(".*[A-Z].*")) score++;
        else suggestions.add("Include at least one uppercase letter (A-Z).");

        if (password.matches(".*[a-z].*")) score++;
        else suggestions.add("Include at least one lowercase letter (a-z).");

        if (password.matches(".*[0-9].*")) score++;
        else suggestions.add("Include at least one number (0-9).");

        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`].*")) score++;
        else suggestions.add("Include at least one special character (!@#$%^&* etc.).");

        String strength = (score == 5) ? "Strong" : (score >= 3) ? "Medium" : "Weak";

        System.out.println("\nPassword Strength: " + strength);
        System.out.println("Score: " + score + "/5");

        if (score == 5) {
            System.out.println("Suggestion: Password meets all security requirements.");
        } else {
            System.out.println("Suggestions for improvement:");
            for (String suggestion : suggestions) {
                System.out.println(" - " + suggestion);
            }
        }
    }

    // -----------------------------------------------------------------
    // 3. Password Generator
    // -----------------------------------------------------------------
    private static void generatePassword() {
        System.out.println("\n--- PASSWORD GENERATOR ---");
        System.out.print("Required Length: ");
        if (!scanner.hasNextLine()) return;
        String input = scanner.nextLine().trim();
        int length;

        try {
            length = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid length entered. Using default length of 12.");
            length = 12;
        }

        if (length < 4) {
            System.out.println("Minimum length for a strong password is 4. Setting length to 4.");
            length = 4;
        }

        String generatedPassword = createRandomPassword(length);
        System.out.println("Generated Password: " + generatedPassword);
    }

    private static String createRandomPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));

        for (int i = 4; i < length; i++) {
            sb.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        char[] charArray = sb.toString().toCharArray();
        for (int i = charArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = charArray[i];
            charArray[i] = charArray[j];
            charArray[j] = temp;
        }

        return new String(charArray);
    }

    // -----------------------------------------------------------------
    // 4. Password Manager
    // -----------------------------------------------------------------
    private static void handlePasswordManager() {
        if (!ensureAuthenticated()) {
            System.out.println("Authentication failed. Cannot access Password Manager.");
            return;
        }

        boolean pmRunning = true;
        while (pmRunning) {
            System.out.println("\n--- PASSWORD MANAGER MENU ---");
            System.out.println("1. Add / Store Credential");
            System.out.println("2. View All Saved Credentials");
            System.out.println("3. Search Credential");
            System.out.println("4. Update Credential");
            System.out.println("5. Delete Credential");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select Option (1-6): ");

            if (!scanner.hasNextLine()) break;
            String pmChoice = scanner.nextLine().trim();

            switch (pmChoice) {
                case "1":
                case "Add":
                case "add":
                case "Store":
                case "store":
                    addCredential();
                    break;
                case "2":
                case "View":
                case "view":
                    viewCredentials();
                    break;
                case "3":
                case "Search":
                case "search":
                    searchCredential();
                    break;
                case "4":
                case "Update":
                case "update":
                    updateCredential();
                    break;
                case "5":
                case "Delete":
                case "delete":
                    deleteCredential();
                    break;
                case "6":
                case "Back":
                case "back":
                case "Exit":
                case "exit":
                    pmRunning = false;
                    break;
                default:
                    System.out.println("Invalid option! Enter a number between 1 and 6.");
            }
        }
    }

    private static void addCredential() {
        System.out.println("\n--- ADD CREDENTIAL ---");
        System.out.print("Website: ");
        if (!scanner.hasNextLine()) return;
        String website = scanner.nextLine().trim();

        System.out.print("Username: ");
        if (!scanner.hasNextLine()) return;
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        if (!scanner.hasNextLine()) return;
        String password = scanner.nextLine().trim();

        if (website.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("Website, Username, and Password cannot be empty.");
            return;
        }

        String encryptedPassword = encrypt(password);

        if (useDatabase) {
            String sql = "INSERT INTO credentials (user_id, website, username, encrypted_password) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser.id);
                pstmt.setString(2, website);
                pstmt.setString(3, username);
                pstmt.setString(4, encryptedPassword);
                pstmt.executeUpdate();
                System.out.println("Credentials encrypted and stored successfully.");
            } catch (SQLException e) {
                System.out.println("Failed to store credential in database: " + e.getMessage());
            }
        } else {
            Credential cred = new Credential(currentUser.memoryCredentials.size() + 1, website, username, encryptedPassword);
            currentUser.memoryCredentials.add(cred);
            saveDataToFile();
            System.out.println("Credentials encrypted and stored successfully.");
        }
    }

    private static List<Credential> getUserCredentials() {
        List<Credential> list = new ArrayList<>();
        if (useDatabase) {
            String sql = "SELECT id, website, username, encrypted_password FROM credentials WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser.id);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    list.add(new Credential(
                            rs.getInt("id"),
                            rs.getString("website"),
                            rs.getString("username"),
                            rs.getString("encrypted_password")
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Error reading credentials from SQL database: " + e.getMessage());
            }
        } else {
            list.addAll(currentUser.memoryCredentials);
        }
        return list;
    }

    private static void viewCredentials() {
        System.out.println("\n--- SAVED CREDENTIALS ---");
        List<Credential> list = getUserCredentials();

        if (list.isEmpty()) {
            System.out.println("No saved credentials found.");
            return;
        }

        System.out.printf("%-5s | %-20s | %-25s | %-25s | %-20s%n", "No.", "Website", "Username", "Encrypted Password", "Decrypted Password");
        System.out.println("----------------------------------------------------------------------------------------------------");

        for (int i = 0; i < list.size(); i++) {
            Credential c = list.get(i);
            String decrypted = decrypt(c.encryptedPassword);
            System.out.printf("%-5d | %-20s | %-25s | %-25s | %-20s%n",
                    (i + 1), c.website, c.username, truncate(c.encryptedPassword, 24), decrypted);
        }
    }

    private static void searchCredential() {
        System.out.println("\n--- SEARCH CREDENTIAL ---");
        System.out.print("Enter Website or Username to search: ");
        if (!scanner.hasNextLine()) return;
        String query = scanner.nextLine().trim().toLowerCase();

        if (query.isEmpty()) {
            System.out.println("Query cannot be empty.");
            return;
        }

        List<Credential> list = getUserCredentials();
        boolean found = false;

        for (Credential c : list) {
            if (c.website.toLowerCase().contains(query) || c.username.toLowerCase().contains(query)) {
                if (!found) {
                    System.out.println("\nMatching Credentials:");
                    System.out.printf("%-20s | %-25s | %-25s | %-20s%n", "Website", "Username", "Encrypted Password", "Decrypted Password");
                    System.out.println("------------------------------------------------------------------------------------");
                    found = true;
                }
                System.out.printf("%-20s | %-25s | %-25s | %-20s%n",
                        c.website, c.username, truncate(c.encryptedPassword, 24), decrypt(c.encryptedPassword));
            }
        }

        if (!found) {
            System.out.println("No credentials matching '" + query + "' found.");
        }
    }

    private static void updateCredential() {
        System.out.println("\n--- UPDATE CREDENTIAL ---");
        List<Credential> list = getUserCredentials();

        if (list.isEmpty()) {
            System.out.println("No credentials to update.");
            return;
        }

        viewCredentials();
        System.out.print("\nEnter the number of the credential to update: ");
        if (!scanner.hasNextLine()) return;
        String input = scanner.nextLine().trim();

        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < list.size()) {
                Credential c = list.get(index);

                System.out.print("Enter new Website (Leave blank to keep '" + c.website + "'): ");
                String newWeb = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                if (newWeb.isEmpty()) newWeb = c.website;

                System.out.print("Enter new Username (Leave blank to keep '" + c.username + "'): ");
                String newUser = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                if (newUser.isEmpty()) newUser = c.username;

                System.out.print("Enter new Password (Leave blank to keep current): ");
                String newPass = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                String newEncryptedPass = newPass.isEmpty() ? c.encryptedPassword : encrypt(newPass);

                if (useDatabase) {
                    String sql = "UPDATE credentials SET website = ?, username = ?, encrypted_password = ? WHERE id = ? AND user_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, newWeb);
                        pstmt.setString(2, newUser);
                        pstmt.setString(3, newEncryptedPass);
                        pstmt.setInt(4, c.id);
                        pstmt.setInt(5, currentUser.id);
                        pstmt.executeUpdate();
                        System.out.println("Credential updated successfully in SQL Database!");
                    } catch (SQLException e) {
                        System.out.println("Database update failed: " + e.getMessage());
                    }
                } else {
                    c.website = newWeb;
                    c.username = newUser;
                    c.encryptedPassword = newEncryptedPass;
                    saveDataToFile();
                    System.out.println("Credential updated successfully!");
                }
            } else {
                System.out.println("Invalid index.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }

    private static void deleteCredential() {
        System.out.println("\n--- DELETE CREDENTIAL ---");
        List<Credential> list = getUserCredentials();

        if (list.isEmpty()) {
            System.out.println("No credentials to delete.");
            return;
        }

        viewCredentials();
        System.out.print("\nEnter the number of the credential to delete: ");
        if (!scanner.hasNextLine()) return;
        String input = scanner.nextLine().trim();

        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < list.size()) {
                Credential c = list.get(index);

                if (useDatabase) {
                    String sql = "DELETE FROM credentials WHERE id = ? AND user_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, c.id);
                        pstmt.setInt(2, currentUser.id);
                        pstmt.executeUpdate();
                        System.out.println("Credential for '" + c.website + "' deleted successfully from SQL Database!");
                    } catch (SQLException e) {
                        System.out.println("Database delete failed: " + e.getMessage());
                    }
                } else {
                    currentUser.memoryCredentials.remove(index);
                    saveDataToFile();
                    System.out.println("Credential for '" + c.website + "' deleted successfully!");
                }
            } else {
                System.out.println("Invalid index.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }

    // -----------------------------------------------------------------
    // Cryptography & Utility Helpers
    // -----------------------------------------------------------------
    public static String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return strToEncrypt;
        }
    }

    public static String decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return strToDecrypt;
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }

    private static String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
