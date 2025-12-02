package Server;

import Enums.AccState;

public class Player extends Account {
    private static int count;

    private String ID;
    private double balance;
    private String sessionID;
    private Hand hand;
    private Bet activeBet;

    public Player(String username, String password, double balance){
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.ID = ("P" + ++count);
        sessionID = null;
        hand = new Hand();
        accountState = AccState.ACTIVE;
        this.activeBet = null;
    }

    public Bet placeBet(double amt){
        activeBet = new Bet(this, amt);
        return activeBet;
    }

    public boolean hit(Card card){
        hand.addCard(card);
        return hand.getValue() > 21; // true = bust
    }

    public void stand(){
        // no-op for now; action handled elsewhere
    }

    public void updateBalance(double amount){
        this.balance += amount;
    }

    public boolean isBust(){
        return hand.getValue() > 21;
    }

    public double getBalance(){
        return balance;
    }

    public int getHandValue(){
        return hand.getValue();
    }

    public Hand getHand(){
        return hand;
    }

    public Bet getBet(){
        return activeBet;
    }

    @Override
    public String getUsername(){
        return username;
    }

    public String getID(){
        return ID;
    }
}

    // put this in test class later
    
    // public static void main(String[] args) {
    //     Player player = new Player ("sam", "sam", 500);
    //     System.out.print(player.placeBet(50).toString());
    // }

