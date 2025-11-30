package Server;

import Enums.Rank;
import java.util.ArrayList;

public class Hand {
    private ArrayList<Card> cards;
    private boolean isActive;
    private int hardValue;
    private int softValue;
    private int aceCount;

    public Hand() {
        cards = new ArrayList<>();
        isActive = false;
        hardValue = 0;
        softValue = 0;
        aceCount = 0;
    }

    public void addCard(Card card) {
        cards.add(card);
        recalculateValue();
    }

    private void recalculateValue() {
        hardValue = 0;
        aceCount = 0;

        for (Card card : cards) {
            if (card.getRank() == Rank.ACE) {
                aceCount++;
                hardValue += 1;
            } else {
                hardValue += card.getValue();
            }
        }

        softValue = hardValue;
        if (aceCount > 0 && hardValue + 10 <= 21) {
            softValue = hardValue + 10;
        }
    }

    public int getValue() {
        return softValue > 21 ? hardValue : softValue;
    }

    public int getHardValue() {
        return hardValue;
    }

    public int getSoftValue() {
        return softValue;
    }

    public boolean isSoft() {
        return softValue > hardValue && softValue <= 21;
    }

    public boolean isBust() {
        return getValue() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getValue() == 21;
    }

    public boolean canSplit() {
        return cards.size() == 2 && cards.get(0).getRank() == cards.get(1).getRank();
    }

    public int getCardCount() {
        return cards.size();
    }

    public ArrayList<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public void clearHand() {
        cards.clear();
        hardValue = 0;
        softValue = 0;
        aceCount = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card.toString()).append(" ");
        }
        sb.append("[Value: ").append(getValue()).append("]");
        return sb.toString();
    }
}
