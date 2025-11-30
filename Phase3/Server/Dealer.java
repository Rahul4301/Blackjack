package Server;

import Enums.AccState;
import Enums.HandEval;

public class Dealer extends Account {
    private static int count;

    private String dealerID;
    private GameTable currentTable;
    private Hand hand;

    public Dealer(String username, String password){
        this.username = username;
        this.password = password;
        dealerID = ("D" + ++count);
        hand = null;
        //currentTable = null;
        sessionActive = false;
        accountState = AccState.ACTIVE;
    }

    
    public boolean hit(Card card){
        hand.addCard(card);
        if(hand.getValue() > 21) return true; //return true = is a BUST
        return false;
    }

    public boolean mustHit(int total){
        return total < 17;
    }


    public String getID(){
        return dealerID;
    }

    public GameTable getTable(){
        return currentTable;
    }

    public Hand getHand(){
        return hand;
    }

       public int getHandValue(){
        return hand.getValue();
    }
    

    public boolean mustHit(int total, boolean soft){
        boolean flag = false;
        //add smthn here to see if player(s) has made move
        return flag;
    }

    public HandEval compareHands(Hand playerHand){
        if (hand.getValue() > playerHand.getValue()) return HandEval.MORE;
        else if (hand.getValue() < playerHand.getValue()) return HandEval.LESS;
        else return HandEval.EQUAL;
    }

}
