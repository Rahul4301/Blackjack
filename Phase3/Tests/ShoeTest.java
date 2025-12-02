package Tests;

import Server.Shoe;
import Server.Card;
import Enums.Rank;
import Enums.Suit;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ShoeTest {

    @Test
    void oneDeckStartsWith52Cards() {
        Shoe shoe = new Shoe(1);
        assertEquals(52, shoe.cardsRemaining());
    }

    @Test
    void multiDeckStartsWithCorrectCount() {
        Shoe shoe = new Shoe(4);
        assertEquals(4 * 52, shoe.cardsRemaining());
    }

    @Test
    void dealCardReducesCount() {
        Shoe shoe = new Shoe(1);
        int start = shoe.cardsRemaining();

        assertNotNull(shoe.dealCard());
        assertEquals(start - 1, shoe.cardsRemaining());
    }

    @Test
    void dealAllCardsThenReturnsNull() {
        Shoe shoe = new Shoe(1);
        for (int i = 0; i < 52; i++) {
            assertNotNull(shoe.dealCard());
        }
        assertNull(shoe.dealCard());
    }

    @Test
    void resetRestoresFullShoe() {
        Shoe shoe = new Shoe(2);
        shoe.dealCard();
        shoe.dealCard();

        shoe.resetShoe();
        assertEquals(2 * 52, shoe.cardsRemaining());
    }

    @Test
    void zeroOrNegativeDecksDefaultsToOne() {
        Shoe zero = new Shoe(0);
        Shoe negative = new Shoe(-3);

        assertEquals(52, zero.cardsRemaining());
        assertEquals(52, negative.cardsRemaining());
    }

    @Test
    void multiDeckContainsCorrectCopies() {
        int n = 2;
        Shoe shoe = new Shoe(n);

        Map<String, Integer> map = new HashMap<>();

        Card c;
        while ((c = shoe.dealCard()) != null) {
            String key = c.getRank().name() + "-" + c.getSuit().name();
            map.put(key, map.getOrDefault(key, 0) + 1);
        }

        for (Rank r : Rank.values()) {
            for (Suit s : Suit.values()) {
                String key = r.name() + "-" + s.name();
                assertEquals(n, map.get(key));
            }
        }
    }

    @Test
    void shoeIDIsStable() {
        Shoe shoe = new Shoe(2);
        assertNotNull(shoe.getShoeID());
        assertEquals(shoe.getShoeID(), shoe.getShoeID());
    }
}
