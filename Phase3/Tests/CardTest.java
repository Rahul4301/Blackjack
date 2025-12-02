package Tests;

import Server.Card;
import Enums.Rank;
import Enums.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

    @Test
    void testRankIsStoredCorrectly() {
        Card card = new Card(Rank.KING, Suit.HEARTS);
        assertEquals(Rank.KING, card.getRank(), "Rank should match constructor argument");
    }

    @Test
    void testSuitIsStoredCorrectly() {
        Card card = new Card(Rank.ACE, Suit.SPADES);
        assertEquals(Suit.SPADES, card.getSuit(), "Suit should match constructor argument");
    }

    @Test
    void testCardValueNumbers() {
        assertEquals(2, new Card(Rank.TWO, Suit.CLUBS).getValue());
        assertEquals(7, new Card(Rank.SEVEN, Suit.DIAMONDS).getValue());
        assertEquals(9, new Card(Rank.NINE, Suit.SPADES).getValue());
    }

    @Test
    void testCardValueFaceCards() {
        assertEquals(10, new Card(Rank.TEN, Suit.HEARTS).getValue());
        assertEquals(10, new Card(Rank.JACK, Suit.SPADES).getValue());
        assertEquals(10, new Card(Rank.QUEEN, Suit.DIAMONDS).getValue());
        assertEquals(10, new Card(Rank.KING, Suit.CLUBS).getValue());
    }

    @Test
    void testCardValueAce() {
        Card ace = new Card(Rank.ACE, Suit.CLUBS);
        assertEquals(11, ace.getValue(), "Ace should default to 11 per implementation");
    }

    @Test
    void testToStringFormat() {
        Card card = new Card(Rank.FOUR, Suit.HEARTS);
        String s = card.toString();

        assertNotNull(s, "toString should not return null");
        assertTrue(s.contains("FOUR"), "toString should include the rank name");
        assertTrue(s.contains("HEARTS"), "toString should include the suit name");
        assertEquals("FOUR of HEARTS", s, "toString should match 'RANK of SUIT' format");
    }
}
