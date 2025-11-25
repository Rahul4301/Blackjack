package Server;

import enums.AccState;

public class Player extends Account{
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
        hand = null;
        accountState = AccState.ACTIVE;
        this.activeBet = null;
    }

    public Bet placeBet(double amt){
        activeBet = new Bet(this, amt);
        return activeBet;
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
        if(hand.getValue() > 21) return true;
        return false;
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

    public String getUsername(){
        return username;
    }

    public String getID(){
        return ID;
    }

    public static void main(String[] args) {
        Player player = new Player ("sam", "sam", 500);
        System.out.print(player.placeBet(50).toString());
    }
}
