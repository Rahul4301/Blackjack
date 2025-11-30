package Client;

import Server.GameTable;
import Server.Player;
import Server.Hand;
import Server.Card;
import java.util.Scanner;
import java.util.Stack;

/**
 * Enhanced console-based Menu for Blackjack client.
 * Provides interactive game flow and table display.
 */
public class Menu {
    private Scanner scanner;
    private boolean isRunning;
    private GameClient client;
    private String currentScreen;
    private String userType;
    private boolean isLoggedIn;
    private String currentUser;
    private Stack<String> menuHistory;
    private double playerBalance;
    private boolean inGame;

    public Menu(GameClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.isRunning = true;
        this.currentScreen = "MAIN";
        this.userType = "guest";
        this.isLoggedIn = false;
        this.currentUser = null;
        this.menuHistory = new Stack<>();
        this.playerBalance = 0;
        this.inGame = false;
    }

    /**
     * Display main menu.
     */
    public void displayMainMenu() {
        while (isRunning && !inGame) {
            printHeader("Blackjack Main Menu");
            if (isLoggedIn) {
                System.out.println("Logged in as: " + currentUser + " | Balance: $" + playerBalance);
                System.out.println("1) Join Table");
                System.out.println("2) View Profile");
                System.out.println("3) Logout");
                System.out.println("4) Exit");
            } else {
                System.out.println("1) Login");
                System.out.println("2) Register");
                System.out.println("3) Exit");
            }
            System.out.print("\nSelect option: ");

            int choice = readInt();
            handleMainMenuSelection(choice);
        }
    }

    /**
     * Handle main menu selections.
     */
    private void handleMainMenuSelection(int choice) {
        if (isLoggedIn) {
            switch (choice) {
                case 1:
                    joinGameTable();
                    break;
                case 2:
                    viewProfile();
                    break;
                case 3:
                    logout();
                    break;
                case 4:
                    exitGame();
                    break;
                default:
                    System.out.println("Invalid selection.");
            }
        } else {
            switch (choice) {
                case 1:
                    loginScreen();
                    break;
                case 2:
                    registerScreen();
                    break;
                case 3:
                    exitGame();
                    break;
                default:
                    System.out.println("Invalid selection.");
            }
        }
    }

    /**
     * Display login screen.
     */
    private void loginScreen() {
        printHeader("Login");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        System.out.println("\nAttempting login...");
        client.login(username, password);
        
        // Simulate successful login
        sleep(500);
        isLoggedIn = true;
        currentUser = username;
        userType = "player";
        playerBalance = 1000;
        System.out.println("✓ Login successful!");
    }

    /**
     * Display register screen.
     */
    private void registerScreen() {
        printHeader("Register New Account");
        System.out.print("New Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty() || username.length() < 3) {
            System.out.println("Username must be at least 3 characters.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty() || password.length() < 3) {
            System.out.println("Password must be at least 3 characters.");
            return;
        }

        System.out.println("\nCreating account...");
        client.register(username, password);
        sleep(500);
        System.out.println("✓ Account created! You can now login.");
    }

    /**
     * Join a game table.
     */
    private void joinGameTable() {
        printHeader("Joining Table");
        System.out.println("Requesting to join a game table...");
        client.joinTable();
        
        // Wait for server response
        sleep(1000);
        
        if (client.getCurrentTable() != null) {
            inGame = true;
            displayGameTable(client.getCurrentTable());
        }
    }

    /**
     * Display active game table with real-time updates.
     */
    public void displayGameTable(GameTable table) {
        while (inGame && client.isConnected()) {
            clearScreen();
            printHeader("⚡ Blackjack Table - " + table.getTableID());
            
            // Display dealer info
            System.out.println("\n📍 DEALER:");
            System.out.println("   Status: " + table.getState());
            
            // Display players at table
            System.out.println("\n👥 PLAYERS AT TABLE:");
            int playerNum = 1;
            for (Player player : table.getPlayers()) {
                String marker = player.getUsername().equals(currentUser) ? " ► " : "   ";
                System.out.println(marker + playerNum + ". " + player.getUsername() + 
                                   " | Balance: $" + player.getBalance() +
                                   " | Hand: " + formatHand(player.getHand()));
                playerNum++;
            }
            
            // Display active bets
            System.out.println("\n💰 ACTIVE BETS:");
            System.out.println("   Bets placed: " + table.getBets().size());
            
            // Game action menu
            System.out.println("\n" + divider("="));
            System.out.println("OPTIONS:");
            System.out.println("1) Hit");
            System.out.println("2) Stand");
            System.out.println("3) Double Down");
            System.out.println("4) Split");
            System.out.println("5) Place Bet");
            System.out.println("6) Leave Table");
            System.out.print("\nSelect action: ");
            
            int action = readInt();
            handleGameAction(action, table);
        }
    }

    /**
     * Handle in-game player actions.
     */
    private void handleGameAction(int action, GameTable table) {
        switch (action) {
            case 1:
                client.performAction("hit");
                System.out.println("→ Hit!");
                break;
            case 2:
                client.performAction("stand");
                System.out.println("→ Stand!");
                break;
            case 3:
                client.performAction("double");
                System.out.println("→ Doubled down!");
                break;
            case 4:
                client.performAction("split");
                System.out.println("→ Split hand!");
                break;
            case 5:
                placeBet();
                break;
            case 6:
                leaveGameTable();
                break;
            default:
                System.out.println("Invalid action.");
        }
        sleep(500);
    }

    /**
     * Place a bet.
     */
    private void placeBet() {
        System.out.print("Enter bet amount: $");
        int amount = readInt();
        
        if (amount <= 0) {
            System.out.println("Bet must be positive.");
            return;
        }
        
        if (amount > playerBalance) {
            System.out.println("Insufficient balance. You have $" + playerBalance);
            return;
        }
        
        client.placeBet(amount);
        playerBalance -= amount;
        System.out.println("✓ Bet placed: $" + amount);
    }

    /**
     * Leave game table.
     */
    private void leaveGameTable() {
        System.out.println("Leaving table...");
        client.leaveTable();
        inGame = false;
        System.out.println("✓ Left table.");
    }

    /**
     * View player profile.
     */
    private void viewProfile() {
        printHeader("Player Profile");
        System.out.println("Username: " + currentUser);
        System.out.println("Balance: $" + playerBalance);
        System.out.println("Status: Online");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Logout user.
     */
    private void logout() {
        System.out.println("Logging out...");
        client.logout();
        isLoggedIn = false;
        currentUser = null;
        System.out.println("✓ Logged out.");
    }

    /**
     * Exit the game.
     */
    private void exitGame() {
        System.out.println("\nThank you for playing Blackjack! Goodbye!");
        isRunning = false;
        try {
            scanner.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Format hand for display.
     */
    private String formatHand(Hand hand) {
        if (hand == null || hand.getCards().size() == 0) {
            return "No cards";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < hand.getCards().size(); i++) {
            Card card = hand.getCards().get(i);
            sb.append(card.getRank().toString().charAt(0)).append("♠");
            if (i < hand.getCards().size() - 1) sb.append(" ");
        }
        sb.append("] = ").append(hand.getValue());
        return sb.toString();
    }

    /**
     * Read integer input safely.
     */
    private int readInt() {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    System.out.print("Please enter a number: ");
                    continue;
                }
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Enter a number: ");
            }
        }
    }

    /**
     * Helper: Print section header.
     */
    private void printHeader(String title) {
        System.out.println("\n" + divider("="));
        System.out.println("  " + title);
        System.out.println(divider("="));
    }

    /**
     * Helper: Print divider.
     */
    private String divider(String character) {
        return character.repeat(50);
    }

    /**
     * Helper: Clear screen (approximate).
     */
    private void clearScreen() {
        for (int i = 0; i < 3; i++) {
            System.out.println();
        }
    }

    /**
     * Helper: Sleep thread safely.
     */
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
