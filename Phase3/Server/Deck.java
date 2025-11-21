package Server;


import enums.Rank;
import enums.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Simple Deck skeleton containing 52 cards.
 */
public class Deck {
    private ArrayList<Card> cards;
    private String deckID;

    public Deck() {
        this.deckID = UUID.randomUUID().toString();
        this.cards = new ArrayList<>();
        resetDeck();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }

    public int cardsRemaining() {
        return cards.size();
    }

    public void resetDeck() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public String getDeckID() {
        return deckID;
    }
}
