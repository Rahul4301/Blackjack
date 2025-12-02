package Client;

import Enums.PlayerAction;
import Enums.GameState;   // add this

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
    /*
    
    
    MAIN MENU
    
    
    
    
    */

    public void displayMainMenu() {
        navigateTo("MAIN");
        while (isRunning) {
            options.clear();
            System.out.println("\n=== Blackjack Main Menu ===");
            if(isLoggedIn){ System.out.println("Welcome, " + currentUser + "!");}
            System.out.println("1) Login"); //done
            System.out.println("2) Register"); //done
            System.out.println("3) Lobby"); //done(?)
            System.out.println("4) Deposit");
            System.out.println("5) Logout");//done
            System.out.println("6) Exit");//done
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
        //System.out.println("2) Leave Table");
        System.out.println("3) Back");
        System.out.print("Select an option: ");

        int opt = readInt();
        switch (opt) {
            case 1:
                System.out.print("Enter table ID to join: ");
                String tableId = scanner.nextLine().trim();

                TableSnapshot snap = client.joinTable(tableId);
                if (snap != null) {
                    System.out.println("Joined table " + snap.getTableId());
                    managePlayerTable(snap);
                    goBack();
                    break;
                } else {
                    System.out.println("Failed to join table.");
                }
                break;

            // case 2:
            //     System.out.print("Leaving table (if joined)...");
            //     client.leaveTable();
            //     break;
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

        currentScreen = "DEALER_LOBBY";
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
        goBack();
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
    public void manageDealerTable(TableSnapshot snapshot) {
        boolean managing = true;

        while (managing) {
            // Always try to pull an updated snapshot first
            TableSnapshot refreshed = client.requestTableState();
            if (refreshed != null) {
                snapshot = refreshed;
            }

            client.displaySnapshot(snapshot);

            System.out.println("1) Start game");
            System.out.println("2) Refresh");      // optional now, but can keep it
            System.out.println("3) Leave table");
            System.out.print("Select: ");

            int choice = readInt();
            switch (choice) {
                case 1:
                    // existing start game logic
                    // (for example send a START_ROUND or whatever you wired)
                    break;
                case 2:
                    // Manual refresh still allowed, but not needed
                    snapshot = client.requestTableState();
                    break;
                case 3:
                    client.leaveTable();
                    managing = false;
                    break;
                default:
                    System.out.println("Not an option.");
                    break;
            }
        }
    }


   
public void managePlayerTable(TableSnapshot snapshot) {
    boolean playing = true;
    GameState lastState = null;
    Double balanceBeforeRound = null;

    while (playing) {
        if (snapshot == null) {
            snapshot = client.requestTableState();
            if (snapshot == null) {
                System.out.println("Could not load table state.");
                return;
            }
        }

        client.displaySnapshot(snapshot);

        // Find "you" in the snapshot
        PlayerView youView = null;
        if (snapshot.getPlayers() != null) {
            for (PlayerView pv : snapshot.getPlayers()) {
                if (pv.isYou()) {
                    youView = pv;
                    break;
                }
            }
        }

        // During BETTING, show and remember balance before betting
        if (snapshot.getState() == GameState.BETTING && youView != null) {
            if (balanceBeforeRound == null) {
                balanceBeforeRound = youView.getBalance();
            }
            System.out.println("Your balance before betting: " + balanceBeforeRound);
        }

        // When we first enter RESULTS, show win/lose and balances
        if (snapshot.getState() == GameState.RESULTS
                && lastState != GameState.RESULTS
                && youView != null) {

            double after = youView.getBalance();

            if (balanceBeforeRound != null) {
                if (after > balanceBeforeRound) {
                    System.out.println(">>> YOU WIN! <<<");
                } else if (after < balanceBeforeRound) {
                    System.out.println(">>> YOU LOSE <<<");
                } else {
                    System.out.println(">>> PUSH (no net change) <<<");
                }

                System.out.println("Balance before betting: " + balanceBeforeRound);
                System.out.println("Balance after round:   " + after);
            } else {
                System.out.println("Round finished. Your balance: " + after);
            }

            // prepare for possible next round
            balanceBeforeRound = after;
        }

        lastState = snapshot.getState();

        // Existing menus for BETTING vs IN_PROGRESS / RESULTS

        if (snapshot.getState() == GameState.BETTING) {
            System.out.println("== Betting phase ==");
            System.out.println("1) Place bet");
            System.out.println("2) Refresh");
            System.out.println("3) Leave table");
            System.out.print("Select: ");

            int choice = readInt();
            switch (choice) {
                case 1:
                    System.out.print("Enter bet amount (whole dollars): ");
                    int betInt = readInt();
                    double amount = betInt;
                    client.placeBet(amount);
                    snapshot = client.requestTableState();
                    break;
                case 2:
                    snapshot = client.requestTableState();
                    break;
                case 3:
                    client.leaveTable();
                    playing = false;
                    break;
                default:
                    System.out.println("Not an option.");
                    break;
            }

        } else {
            System.out.println("1) Hit");
            System.out.println("2) Stand");
            System.out.println("3) Double down");
            System.out.println("4) Refresh");
            System.out.println("5) Leave table");
            System.out.print("Select: ");

            int choice = readInt();
            switch (choice) {
                case 1:
                    snapshot = client.sendPlayerAction(PlayerAction.HIT);
                    break;
                case 2:
                    snapshot = client.sendPlayerAction(PlayerAction.STAND);
                    break;
                case 3:
                    snapshot = client.sendPlayerAction(PlayerAction.DOUBLE);
                    break;
                case 4:
                    snapshot = client.requestTableState();
                    break;
                case 5:
                    client.leaveTable();
                    playing = false;
                    break;
                default:
                    System.out.println("Not an option.");
                    break;
            }
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
