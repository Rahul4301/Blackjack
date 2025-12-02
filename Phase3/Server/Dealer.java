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
        hand = new Hand();
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
    public boolean isBust() {
        return hand.getValue() > 21;
    }

    public HandEval compareHands(Hand playerHand){
        int dealerValue = hand.getValue();
        int playerValue = playerHand.getValue();

        if (dealerValue > playerValue) {
            return HandEval.MORE;
        } else if (dealerValue < playerValue) {
            return HandEval.LESS;
        } else {
            return HandEval.EQUAL;
        }
    }

}
