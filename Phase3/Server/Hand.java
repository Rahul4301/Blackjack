package Server;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private ArrayList<Card> cards;
    private boolean isActive;
    private int handValue;

    public Hand(){
        cards = new ArrayList<>(64);
        isActive = false;
        handValue = 0;
    }

    public void addCard(Card card){
        cards.add(card);
        return;
    }

    public int getValue(){
        for (Card card : cards){
            handValue += card.getValue();
        }
        return handValue;
    }

    public boolean isBust(){
        boolean flag = false;
        if (handValue > 21) flag = true;

        return flag;
    }

    public boolean isBlackjack(){
        boolean flag = false;
        if (handValue == 21) flag = true;

        return flag;
    }

    public void clearHand(){
        cards.clear();
        return;
    }

    public String toString(){
        for (Card card : cards){
            card.toString();
        }
        return "";
    }
}
