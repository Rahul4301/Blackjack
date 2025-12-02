package Server;

import Enums.Rank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hand implements Serializable {
    private ArrayList<Card> cards;
    private boolean isActive;
    private int handValue;

    public Hand() {
        cards = new ArrayList<>(64);
        isActive = false;
        handValue = 0;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Calculates the hand value, treating Aces as 11 by default,
     * then reducing them to 1 (subtracting 10) as needed to avoid busting.
     */
    public int getValue() {
        int total = 0;
        int aceCount = 0;

        // First, count all cards assuming Ace = 11
        for (Card card : cards) {
            int value = card.getValue();
            total += value;
            // Assuming Card has some way to identify an Ace.
            // If not, adjust this condition to match your Card implementation.
            if (card.getRank() == Rank.ACE) {
                aceCount++;
            }
        }

        // While we're busting and have Aces counted as 11, turn them into 1
        // by subtracting 10 for each Ace.
        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }

        this.handValue = total;
        return handValue;
    }

    public boolean isBust() {
        return getValue() > 21;
    }

    public boolean isBlackjack() {
        // Standard blackjack: exactly 21 with 2 cards.
        // If you want "21 with any number of cards" to count, remove the size check.
        return cards.size() == 2 && getValue() == 21;
    }

    public void clearHand() {
        cards.clear();
        handValue = 0;
        isActive = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Hand: ");
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).toString());
            if (i < cards.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" (value = ").append(getValue()).append(")");
        return sb.toString();
    }

    public List<Card> getCards() {
        return cards;
    }

    // Optional: getters/setters for isActive if you use them elsewhere
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}