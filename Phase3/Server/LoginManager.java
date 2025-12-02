package Server;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class LoginManager {
    // final since we (the developers) determine where data should be stored, not user
    private final String sourceName;
    private ArrayList<Account> accounts;
    private int numAccounts;
    private boolean modified;

    // Saved file data convention: [username],[password],[accType]

    public LoginManager(){
        accounts = new ArrayList<>(64);
        numAccounts = 0;
        sourceName = "awesomeDB.txt";
        modified = false;
    }

    public synchronized void loadData(){
        File file = new File(sourceName);
        // Create the file if it doesn't exist to prevent FileNotFoundException
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("Database file created: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Could not create database file: " + e.getMessage());
                return;
            }
        }
        
        System.out.println("Loading data from: " + file.getAbsolutePath());
        
        try{
            Scanner scan = new Scanner(file);
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                if(line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                String[] words = line.split(",");
                // Ensure there are at least 3 parts: username, password, type
                if (words.length < 3) {
                    System.err.println("Skipping malformed line in DB: " + line);
                    continue;
                }

                String username = words[0];
                String password = words[1];
                String type = words[2];

                if(type.equalsIgnoreCase("Player")){
                    // Assuming default balance is 1000 if not specified in file (Phase 3 likely only stores basic credentials)
                    accounts.add(new Player(username, password, 1000)); 
                } else if(type.equalsIgnoreCase("Dealer")){
                    accounts.add(new Dealer(username, password));
                } else {
                    System.err.println("Invalid account type encountered: " + type);
                }
            }
            scan.close();
            numAccounts = accounts.size();
            modified = false;
        } catch(IOException e) {
            System.err.println("I/O Error loading data: " + e.getMessage());
        } catch(Exception e) {
            System.err.println("General error loading data: " + e.getMessage());
        }
    }

    // Save to file
    public synchronized void save(){
        if (!modified) {
            System.out.println("No changes detected. Skipping save.");
            return;
        }

        try(FileWriter writer = new FileWriter(sourceName)){
            for(Account account : accounts){
                String type = getTypeString(account);
                // Include account state in the saved data for completeness, though only type/creds are currently used
                writer.write(account.getUsername() + "," + account.password + "," + type);
                writer.write("\n");
            }
            modified = false;
            System.out.println("All account data saved successfully.");
        } catch(IOException e) {
            System.err.print("\nSave Error: Failed to write to file!\n");
            e.printStackTrace();
        }
    }

    // Create account and add to ArrayList accounts 
    public synchronized Account createAccount(String username, String password, String type){
        for (Account account : accounts){
            if (account.getUsername().equalsIgnoreCase(username)){
                throw new IllegalArgumentException("Username already exists: " + username);
            }
        }
        Account newAccount;
        switch (type.toUpperCase()){
            case "PLAYER":
                newAccount = new Player(username, password, 1000);
                accounts.add(newAccount);
                numAccounts++;
                break;
            case "DEALER":
                newAccount = new Dealer(username, password);
                accounts.add(newAccount);
                numAccounts++;
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
        modified = true;
        System.out.println("Account created: " + username + " as " + type);
        return newAccount;
    }

    // Convenience method for creating a Player account
    public synchronized Account createPlayerAccount(String username, String password) {
        for (Account account : accounts){
            if (account.getUsername().equalsIgnoreCase(username)){
                throw new IllegalArgumentException("Username already exists: " + username);
            }
        }
        Player newPlayer = new Player(username, password, 1000);
        accounts.add(newPlayer);
        numAccounts++;
        modified = true;
        System.out.println("Player account created: " + username);
        return newPlayer;
    }

    // Corrected login: removed unnecessary try-catch block and exception throwing.
    public synchronized Account login(String username, String password){
        for(Account account : accounts){
            if (account.getUsername().equals(username) && account.password.equals(password)){
                // Assume Account class has a protected/public field for sessionActive
                account.sessionActive = true; 
                System.out.println("User " + username + " logged in.");
                return account;
            }
        }
        System.out.print("\nInvalid username / password!\n");
        return null; // Return null if not found
    }

    // Implemented logout logic
    public synchronized void logout(Account account){
        if (account != null) {
            account.sessionActive = false;
            System.out.println("User " + account.getUsername() + " logged out.");
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Account account : accounts){
            String type = getTypeString(account);
            sb.append("User: ").append(account.getUsername());
            sb.append(" | Password: ").append(account.password); // Assuming password is required for toString for debug
            sb.append(" | Type: ").append(type);
            sb.append(" | Active: ").append(account.sessionActive);
            sb.append("\n");
        }
        return sb.toString();
    }

    // Helper
    private String getTypeString(Account a) {
        if (a instanceof Player) {
            return "Player";
        } else if (a instanceof Dealer) {
            return "Dealer";
        } else {
            return "UNKNOWN";
        }
    }

    // Driver (for testing LoginManager only)
    public static void main(String[] args) {
        LoginManager manager = new LoginManager();
        manager.loadData();
        System.out.println("Before Changes: \n" + manager.toString());
        
        try{
            manager.createAccount("testplayer", "pass123", "player");
            manager.createAccount("testdealer", "pass456", "dealer");
            System.out.println("\n--- After Creation ---");
            System.out.println(manager.toString());
        } catch (IllegalArgumentException e) {
            System.out.println("Error caught: " + e.getMessage());
        }

        Account loggedIn = manager.login("testplayer", "pass123");
        if (loggedIn != null) {
            System.out.println("\nLogin Successful. Session Active: " + loggedIn.sessionActive);
            manager.logout(loggedIn);
            System.out.println("Logout Initiated. Session Active: " + loggedIn.sessionActive);
        } else {
            System.out.println("Login Failed.");
        }
        
        manager.save();
    }
}