package Server;

import enums.AccState;

public class Player extends Account{
    private double balance;
    private String sessionID;
    private Hand hand;
    //Bet activeBet;  TODO: Make Bet class

    public Player(String username, String password, double balance){
        this.username = username;
        this.password = password;
        this.balance = balance;
        sessionID = null;
        hand = null;
        accState = AccState.ACTIVE;
        //Bet activeBet = null;
    }

    public boolean placeBet(double amt){
        //TODO: Create placeBet
        return true; //return true if bet recieved by table
    }

    public void hit(Card card){
        hand.addCard(card);
        return; //Send Message type "PlayerAction" (hit) to table
    }

    public void stand(){
        return; //Send Message type "PlayerAction" to do nothing
    }

    public void doubleDown(){
        return; //Send message PlayerAction
    }

    public void split(){
        return;
    }

    public void updateBalance(double amount){
        this.balance = amount;
        return;
    }

    public boolean isBust(){
        return true; // received from server
    }

    public double getBalance(){
        return balance;
    }

    public int getHandValue(){
        //Evaluate hand value 
        return 0;
    }
}
