package Client;


import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import Server.Client;

/**
 * Simple console-based Menu class for Blackjack client interaction.
 * Assumes a `Client` type exists elsewhere in the project.
 */
public class Menu {
    private Scanner scanner;
    private ArrayList<String> options;
    private boolean isRunning;
    private Client client;
    private String currentScreen;
    private String userType;
    private boolean isLoggedIn;
    private String currentUser;
    private Stack<String> menuHistory;

    public Menu(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.options = new ArrayList<>();
        this.isRunning = true;
        this.currentScreen = "MAIN";
        this.userType = "guest";
        this.isLoggedIn = false;
        this.currentUser = null;
        this.menuHistory = new Stack<>();
    }

    public void displayMainMenu() {
        navigateTo("MAIN");
        while (isRunning) {
            options.clear();
            System.out.println("\n=== Blackjack Main Menu ===");
            System.out.println("1) Login");
            System.out.println("2) Player Lobby");
            System.out.println("3) Deposit");
            System.out.println("4) Logout");
            System.out.println("5) Exit");
            System.out.print("Select an option: ");

            int opt = readInt();
            handleSelection(opt);
        }
    }

    public void displayLoginScreen() {
        navigateTo("LOGIN");
        System.out.println("\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Invalid username.");
            goBack();
            return;
        }

        // NOTE: replace this with actual client authentication flow
        this.isLoggedIn = true;
        this.currentUser = username;
        this.userType = "player";
        System.out.println("Logged in as: " + username);
        goBack();
    }

    public void displayPlayerLobby() {
        if (!isLoggedIn) {
            System.out.println("Please login first.");
            return;
        }

        navigateTo("LOBBY");
        System.out.println("\n--- Player Lobby ---");
        System.out.println("Welcome, " + currentUser + "!");
        System.out.println("1) Join Table");
        System.out.println("2) Leave Table");
        System.out.println("3) Back");
        System.out.print("Select an option: ");

        int opt = readInt();
        switch (opt) {
            case 1:
                System.out.println("Requesting to join a table...");
                // client.joinTable(...) // integrate with Client
                break;
            case 2:
                System.out.println("Leaving table (if joined)...");
                break;
            default:
                goBack();
                break;
        }
    }

    public void handleSelection(int option) {
        switch (currentScreen) {
            case "MAIN":
                switch (option) {
                    case 1:
                        displayLoginScreen();
                        break;
                    case 2:
                        displayPlayerLobby();
                        break;
                    case 3:
                        System.out.println("Deposit feature not implemented yet.");
                        break;
                    case 4:
                        logout();
                        break;
                    case 5:
                        exitGame();
                        break;
                    default:
                        System.out.println("Invalid selection.");
                }
                break;
            default:
                System.out.println("Unhandled screen: " + currentScreen);
        }
    }

    public void navigateTo(String screen) {
        if (currentScreen != null) {
            menuHistory.push(currentScreen);
        }
        currentScreen = screen;
    }

    public void goBack() {
        if (!menuHistory.isEmpty()) {
            currentScreen = menuHistory.pop();
        } else {
            currentScreen = "MAIN";
        }
    }

    public void logout() {
        if (!isLoggedIn) {
            System.out.println("No user is currently logged in.");
            return;
        }
        System.out.println("Logging out " + currentUser + "...");
        this.isLoggedIn = false;
        this.currentUser = null;
        this.userType = "guest";
        // notify client if necessary: client.logout();
    }

    public void exitGame() {
        System.out.println("Exiting game. Goodbye!");
        this.isRunning = false;
        try {
            scanner.close();
        } catch (Exception ignored) {
        }
    }

    private int readInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
