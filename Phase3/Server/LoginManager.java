package Server;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class LoginManager {
    //final since we (the developers) determine where data should be stored, not user
    private final String sourceName;
    private ArrayList<Account> accounts;
    private int numAccounts;
    private boolean modified;

    //Saved file data convention: [username],[password],[balance],[accType]
    // For Dealer, balance is 0

    public LoginManager(){
        accounts = new ArrayList<>(64);
        numAccounts = 0;
        sourceName = "awesomeDB.txt";
        modified = false;
    }

    public void loadData(){
        File file = new File(sourceName);
        if (!file.exists()) {
            System.out.println("[LoginManager] No database file found: " + file.getAbsolutePath());
            return;
        }
        
        System.out.println("[LoginManager] Loading accounts from: " + file.getAbsolutePath());
        try (Scanner scan = new Scanner(file)) {
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                if(line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] words = line.split(",");
                if (words.length < 4) {
                    System.err.println("[LoginManager] Skipping invalid line: " + line);
                    continue;
                }
                
                String username = words[0];
                String password = words[1];
                double balance;
                try {
                    balance = Double.parseDouble(words[2]);
                } catch (NumberFormatException e) {
                    System.err.println("[LoginManager] Invalid balance for " + username + ": " + words[2]);
                    balance = 0;
                }
                String type = words[3];
                
                if(type.equalsIgnoreCase("Player")){
                    accounts.add(new Player(username, password, balance));
                } else if(type.equalsIgnoreCase("Dealer")){
                    accounts.add(new Dealer(username, password));
                } else {
                    System.err.println("[LoginManager] Invalid account type: " + type);
                }
            }
            numAccounts = accounts.size();
            modified = false;
            System.out.println("[LoginManager] Loaded " + numAccounts + " accounts.");
        } catch(IOException e) {
            System.err.println("[LoginManager] Error loading data: " + e.getMessage());
        }
    }

    //Save to file
    public void save(){
        try(FileWriter writer = new FileWriter(sourceName)){
            for(Account account : accounts){
                String type = getTypeString(account);
                double balance = (account instanceof Player) ? ((Player) account).getBalance() : 0;
                writer.write(account.username + "," + account.password + "," + balance + "," + type);
                writer.write("\n");
            }
            modified = false;
            System.out.println("[LoginManager] Saved " + accounts.size() + " accounts to " + sourceName);
        } catch(IOException e) {
            System.err.println("[LoginManager] Save Error: " + e.getMessage());
        }
    }

    //Create account and add to ArrayList accounts 
    //Note: when using createAccount, surround it in "try - catch" block bc of exception throwing
    public void createAccount(String username, String password, String type){
        for (Account account : accounts){
            if (account.username.equalsIgnoreCase(username)){
                throw new IllegalArgumentException("Username already exists: " + username);
            }
        }

        switch (type.toUpperCase()){
            case "PLAYER":
                accounts.add(new Player(username, password, 1000));
                break;
            case "DEALER":
                accounts.add(new Dealer(username, password));
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
        modified = true;
    }

    public Account login(String username, String password){
        try {
            for(Account account : accounts){
                if (account.username.equals(username) && account.password.equals(password)){
                    account.sessionActive = true;
                    return account;
                }
            }
            throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            System.out.print("\nInvalid username / password!\n");
            return null;
        }
    }

    /**
     * Authenticate is an alias for login.
     */
    public Account authenticate(String username, String password) {
        return login(username, password);
    }

    /**
     * Logout an account.
     */
    public void logout(Account account) {
        if (account != null) {
            account.sessionActive = false;
            modified = true;
        }
    }

    @Override
    public String toString(){
        String all = "";
        for(Account account : accounts){
            all += ("User: " + account.username +  " | Password: " + account.password + " | Type: " + getTypeString(account) + "\n");
        }

        return all;
    }

    //Helper
    private String getTypeString(Account a) {
        if (a instanceof Player) {
            return "PLAYER";
        } else if (a instanceof Dealer) {
            return "DEALER";
        } else {
            return "UNKNOWN";
        }
    }

    //Driver
    public static void main(String[] args) {
        LoginManager manager = new LoginManager();
        manager.loadData();
        System.out.println("Before: \n" + manager.toString());
        try{
            manager.createAccount("smsms", "msmsms", "player");
            System.out.println("After: \n");
            System.out.println(manager.toString());
        } catch (IllegalArgumentException e) {
            System.out.println("Error caught!");
        }
    }
}
