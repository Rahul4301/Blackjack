package Tests;

import Server.Hand;
import Server.Card;
import Enums.Rank;
import Enums.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HandTest {

    @Test
    void testEmptyHandHasValueZero() {
        Hand hand = new Hand();
        assertEquals(0, hand.getValue(), "Empty hand should have value 0");
        assertFalse(hand.isBust(), "Empty hand should not be bust");
        assertFalse(hand.isBlackjack(), "Empty hand should not be blackjack");
    }

    @Test
    void testAddCardAndGetValue_NoAces() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.TEN, Suit.SPADES));
        hand.addCard(new Card(Rank.FIVE, Suit.HEARTS));

        assertEquals(15, hand.getValue(), "10 + 5 should equal 15");
        assertFalse(hand.isBust(), "15 should not be bust");
    }

    @Test
    void testSingleAce_NoBust() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));   // 11
        hand.addCard(new Card(Rank.SIX, Suit.DIAMONDS)); // 6

        assertEquals(17, hand.getValue(), "Ace + 6 should be 17");
        assertFalse(hand.isBust(), "17 should not be bust");
    }

    @Test
    void testSingleAce_ConvertedToAvoidBust() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));   // 11
        hand.addCard(new Card(Rank.NINE, Suit.HEARTS)); // 9
        hand.addCard(new Card(Rank.KING, Suit.SPADES)); // 10

        // 11 + 9 + 10 = 30 → Ace becomes 1 → 20
        assertEquals(20, hand.getValue(), "Ace should convert from 11 to 1 to avoid bust");
        assertFalse(hand.isBust(), "20 should not be bust");
    }

    @Test
    void testTwoAcesConversion() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));    // 11
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));   // 11
        hand.addCard(new Card(Rank.NINE, Suit.DIAMONDS)); // 9

        // 11 + 11 + 9 = 31 → one Ace becomes 1 → 21
        assertEquals(21, hand.getValue(), "Two Aces + 9 should reduce to 21");
        assertFalse(hand.isBust(), "21 should not be bust");
    }

    @Test
    void testBustWithoutAces() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.KING, Suit.CLUBS));    // 10
        hand.addCard(new Card(Rank.QUEEN, Suit.DIAMONDS)); // 10
        hand.addCard(new Card(Rank.FIVE, Suit.HEARTS));    // 5

        assertEquals(25, hand.getValue(), "10 + 10 + 5 should be 25");
        assertTrue(hand.isBust(), "25 should be bust");
    }

    @Test
    void testIsBlackjackTrue() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));
        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));

        assertEquals(21, hand.getValue(), "Ace + 10 should be 21");
        assertTrue(hand.isBlackjack(), "Two-card 21 should be blackjack");
    }

    @Test
    void testIsBlackjackFalse_WithThreeCards21() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.SEVEN, Suit.CLUBS));
        hand.addCard(new Card(Rank.SEVEN, Suit.DIAMONDS));
        hand.addCard(new Card(Rank.SEVEN, Suit.HEARTS)); // 21, but 3 cards

        assertEquals(21, hand.getValue(), "7 + 7 + 7 should be 21");
        assertFalse(hand.isBlackjack(), "21 with 3 cards is not blackjack by this implementation");
    }

    @Test
    void testClearHandResetsState() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.TEN, Suit.SPADES));
        hand.addCard(new Card(Rank.FIVE, Suit.CLUBS));
        hand.setActive(true);

        assertEquals(2, hand.getCards().size(), "Hand should start with 2 cards");
        assertTrue(hand.isActive(), "Hand should be active before clear");

        hand.clearHand();

        assertEquals(0, hand.getCards().size(), "Hand should be empty after clear");
        assertEquals(0, hand.getValue(), "Hand value should be 0 after clear");
        assertFalse(hand.isActive(), "Hand should not be active after clear");
    }

    @Test
    void testIsActiveGetterSetter() {
        Hand hand = new Hand();
        assertFalse(hand.isActive(), "New hand should not be active by default");

        hand.setActive(true);
        assertTrue(hand.isActive(), "Hand should be active after setActive(true)");

        hand.setActive(false);
        assertFalse(hand.isActive(), "Hand should be inactive after setActive(false)");
    }

    @Test
    void testToStringIncludesValue() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.SPADES));
        hand.addCard(new Card(Rank.FOUR, Suit.HEARTS));

        String s = hand.toString();
        assertNotNull(s, "toString should not return null");
        assertTrue(s.startsWith("Hand:"), "toString should start with 'Hand:'");
        assertTrue(s.contains("value"), "toString should include the hand value");
    }
}
