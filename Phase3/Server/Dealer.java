package Server;

import enums.AccState;
import enums.HandEval;

public class Dealer extends Account {
    private static int count;

    private int dealerID;
    //GameTable currentTable;
    private Hand hand;

    public Dealer(String username, String password){
        this.username = username;
        this.password = password;
        dealerID = ++count;
        hand = null;
        //currentTable = null;
        sessionActive = false;
        accountState = AccState.ACTIVE;
    }

    // public void prepareRound(GameTable table){
    //     return;
    // }

    // public void initialDeal(GameTable table){
    //     return;
    // }

    // public void playDealerTurn(GameTable table){
    //     return;
    // }

    // public void resolveRound(GameTable table){
    //     return;
    // }

    // public void payout(GameTable table){
    //     return;
    // }

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
