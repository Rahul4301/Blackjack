

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Shoe that contains multiple decks.
 */
public class Shoe {
    private ArrayList<Deck> decks;
    private String shoeID;
    private int numDecks;

    public Shoe(int numDecks) {
        this.numDecks = Math.max(1, numDecks);
        this.shoeID = UUID.randomUUID().toString();
        this.decks = new ArrayList<>();
        for (int i = 0; i < this.numDecks; i++) {
            decks.add(new Deck());
        }
        shuffleAll();
    }

    public void shuffleAll() {
        for (Deck d : decks) {
            d.shuffle();
        }
        Collections.shuffle(decks);
    }

    public Card dealCard() {
        for (Deck d : decks) {
            Card c = d.dealCard();
            if (c != null) {
                return c;
            }
        }
        return null; // no cards left
    }

    public int cardsRemaining() {
        int total = 0;
        for (Deck d : decks) {
            total += d.cardsRemaining();
        }
        return total;
    }

    public void resetShoe() {
        decks.clear();
        for (int i = 0; i < this.numDecks; i++) {
            decks.add(new Deck());
        }
        shuffleAll();
    }

    public String getShoeID() {
        return shoeID;
    }
}
