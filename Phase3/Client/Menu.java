package Client;


import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import Shared.*;


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
            if(isLoggedIn){ System.out.println("Welcome, " + currentUser + "!");}
            System.out.println("1) Login");
            System.out.println("2) Register");
            System.out.println("3) Lobby");
            System.out.println("4) Deposit");
            System.out.println("5) Logout");
            System.out.println("6) Exit");
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

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (client.login(username, password)) {
            this.isLoggedIn = true;
            // if you want to mirror the actual account info:
            if (client.getAccount() != null) {
                this.currentUser = client.getAccount().getUsername();
            } else {
                this.currentUser = username;
            }
            if(client.getAccount() instanceof Server.Dealer dealer){    // or derive from Account if you have a type field
                this.userType = "DEALER";
            } else if(client.getAccount() instanceof Server.Player player){
                this.userType = "PLAYER";
            } else {
                System.out.println("Must be a player or dealer!");
            }
            System.out.println("Logged in as: " + currentUser);
        } else {
            System.out.println("Login failed.");
        }
        goBack();
    }

    public void displayRegisterScreen(){
        navigateTo("REGISTER");
        System.out.println("\n--- Register New Account ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()){
            System.out.println("Invalid username.");
            goBack();
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        System.out.print("Account type: ");
        String type = scanner.nextLine().trim();

        if(client.register(username, password, type)){
            this.isLoggedIn = true;

            if (client.getAccount() != null){
                this.currentUser = client.getAccount().getUsername();
            } else {
                this.currentUser = username;
            }
        } else {
            System.out.println("Registration failed.");
            goBack();
            return;
        }
        this.userType = type;
        goBack();
    }

    public void displayPlayerLobby() {
        if (!client.isLoggedIn()) {
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

    public void displayDealerLobby() {
        if (!isLoggedIn) {
            System.out.println("Please login first.");
            return;
        }

        if (!"dealer".equalsIgnoreCase(userType)) {
            System.out.println("Dealer lobby is only for dealers.");
            return;
        }

        navigateTo("DEALER_LOBBY");
        System.out.println("\n--- Dealer Lobby ---");
        System.out.println("Welcome, " + currentUser + "!");
        System.out.println("1) Create new table");
        System.out.println("2) Leave current table");
        System.out.println("3) Back");
        System.out.print("Select an option: ");

        int opt = readInt();
        switch (opt) {
            case 1:
                System.out.println("Requesting to create a new table...");
                TableSnapshot snapshot = client.createTable();
                if (snapshot != null) {
                    // Enter table view and stay there until dealer presses 4
                    manageDealerTable(snapshot);
                }
                break;
            case 2:
                System.out.println("Requesting to leave current table...");
                // send LEAVE_TABLE here when you implement it
                break;
            case 3:
            default:
                goBack();
                break;
        }
        // remove the extra goBack() here, or you will pop one level
    }


    public void handleSelection(int option) {
        switch (currentScreen) {
            case "MAIN":
                switch (option) {
                    case 1:
                        displayLoginScreen();
                        break;
                    case 2:
                        displayRegisterScreen();
                        break;
                    case 3:
                        if (userType.equalsIgnoreCase("DEALER")) {
                            displayDealerLobby();
                        } else {
                            displayPlayerLobby();
                        }
                        break;
                    case 4:
                        System.out.println("Deposit feature not implemented yet.");
                        break;
                    case 5:
                        logout();
                        break;
                    case 6:
                        exitGame();
                        break;
                    default:
                        System.out.println("Invalid selection.");
                }
                break;

            case "DEALER_LOBBY":
                switch (option) {
                    case 1:
                        System.out.println("Requesting to create a new table...");
                        var snap = client.createTable();
                        if (snap != null) {
                            // Here you can go into your table management loop
                            manageDealerTable(snap);
                        }
                        break;
                    case 2:
                        System.out.println("Requesting to leave current table...");
                        // client.leaveTable() when you implement it
                        break;
                    case 3:
                        goBack();   // returns to previous screen, probably MAIN
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
        if (!client.isLoggedIn()) {
            System.out.println("No user is currently logged in.");
            return;
        }

        System.out.println("Logging out " + currentUser + "...");
        client.logout();

        this.isLoggedIn = false;
        this.currentUser = null;
        this.userType = "guest";
    }


    public void exitGame() {
        System.out.println("Exiting game. Goodbye!");
        this.isRunning = false;
        try {
            scanner.close();
        } catch (Exception ignored) {
        }
    }

    //More snapshot stuff
    private void manageDealerTable(TableSnapshot snapshot) {
        boolean managing = true;
        while (managing) {
            System.out.println();
            System.out.println("=== Current table view ===");

            // If you put displaySnapshot in Client:
            client.displaySnapshot(snapshot);

            // If displaySnapshot is instead in Menu, call displaySnapshot(snapshot) directly

            System.out.println();
            System.out.println("3) Start game");
            System.out.println("4) Back to main menu");
            System.out.print("Select an option: ");

            int choice = readInt();
            switch (choice) {
                case 3:
                    // Only allow if there is at least one player at the table
                    if (snapshot.getPlayers() == null || snapshot.getPlayers().isEmpty()) {
                        System.out.println("You cannot start the game. No players have joined this table yet.");
                    } else {
                        // Ask server to start the round and return a fresh snapshot
                        // TableSnapshot newSnap = client.startRound(snapshot.getTableId());
                        // if (newSnap != null) {
                        //     snapshot = newSnap;  // update view
                        // }
                    }
                    break;
                case 4:
                    managing = false;   // exit table view loop
                    break;
                default:
                    System.out.println("Invalid option. Please choose 3 or 4.");
                    break;
            }
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
