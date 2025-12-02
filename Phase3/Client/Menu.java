package Client;


import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;


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
                System.out.print("Enter table ID to join: ");
                String tableId = scanner.nextLine().trim();
                if (tableId.isEmpty()){
                    tableId = "T1";
                }
                client.joinTable(tableId);
                break;
            case 2:
                System.out.print("Leaving table (if joined)...");
                client.leaveTable();
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
                client.createTable();
                break;
            case 2:
                client.leaveTable();
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
                        displayRegisterScreen();
                        break;
                    case 3:
                        if(userType.equalsIgnoreCase("DEALER")){
                            displayDealerLobby();
                            break;
                        }
                        displayPlayerLobby();
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
