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

    //Saved file data convention: [username],[password],[accType]

    public LoginManager(){
        accounts = new ArrayList<>(64);
        numAccounts = 0;
        sourceName = "awesomeDB.txt";
        modified = false;
    }

    public void loadData(){
        File file = new File(sourceName);
        System.out.println(file.getAbsolutePath());
        try{
            Scanner scan = new Scanner(file);
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                if(line.isEmpty()) {
                    break;
                }
                String[] words = line.split(",");
                if(words[2].equalsIgnoreCase("Player")){
                    accounts.add(new Player(words[0], words[1], 1000));
                } else if(words[2].equalsIgnoreCase("Dealer")){
                    accounts.add(new Dealer(words[0], words[1]));
                } else {
                    throw new IllegalArgumentException("Invalid account type: " + words[2]);
                }
            }
            scan.close();
            numAccounts = accounts.size();
            modified = false;
        } catch(IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    //Save to file
    public void save(){
        try(FileWriter writer = new FileWriter(sourceName)){
            for(Account account : accounts){
                String type = getTypeString(account);
                writer.write(account.username + "," + account.password + "," + type);
                writer.write("\n");
            }
            modified = false;
        } catch(IOException e) {
            System.out.print("\nSave Error!\n");
            e.printStackTrace();
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
