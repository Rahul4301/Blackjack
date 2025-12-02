import Server.Hand;
import Server.Card;
import Enums.Rank;
import Enums.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HandTest {

    @Test
    void testAddCardAndGetValue_NoAces() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.TEN, Suit.SPADES));
        hand.addCard(new Card(Rank.FIVE, Suit.HEARTS));

        assertEquals(15, hand.getValue(), "10 + 5 should equal 15");
    }

    @Test
    void testSingleAce_NoBust() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS)); // 11
        hand.addCard(new Card(Rank.SIX, Suit.DIAMOND)); // 6

        assertEquals(17, hand.getValue(), "Ace + 6 should be 17");
    }

    @Test
    void testSingleAce_WithBustConversion() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));  // 11
        hand.addCard(new Card(Rank.NINE, Suit.HEARTS)); // 9
        hand.addCard(new Card(Rank.KING, Suit.SPADES)); // 10 → total = 30 → Ace becomes 1 → 20

        assertEquals(20, hand.getValue(), "Ace should convert to 1 to avoid bust");
    }

    @Test
    void testTwoAcesConversion() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));   // 11
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));  // 11
        hand.addCard(new Card(Rank.NINE, Suit.DIAMOND)); // 9

        // 11 + 11 + 9 = 31 → convert ACE to 1 → 21
        assertEquals(21, hand.getValue(), "Two aces should reduce properly to avoid bust");
    }

    @Test
    void testMultipleAcesStillBust() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));   // 11
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));  // 11
        hand.addCard(new Card(Rank.TEN, Suit.CLUBS));   // 10
        hand.addCard(new Card(Rank.NINE, Suit.SPADES)); // 9

        // 11 + 11 + 10 + 9 = 41
        // convert one ace → 31
        // convert second ace → 21
        // add 9 earlier means actual total = 21 + 9? No—careful:
        // 11 + 11 + 10 + 9 = 41 → Aces reduce twice → 41 - 20 = 21
        assertEquals(21, hand.getValue());
    }

    @Test
    void testBust() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.KING, Suit.CLUBS));
        hand.addCard(new Card(Rank.QUEEN, Suit.DIAMOND));
        hand.addCard(new Card(Rank.FIVE, Suit.HEARTS)); // 10 + 10 + 5 = 25

        assertTrue(hand.isBust(), "Hand should be busted at 25");
    }

    @Test
    void testBlackjackTrue() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));
        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));

        assertTrue(hand.isBlackjack(), "Ace + 10 = Blackjack");
    }

    @Test
    void testBlackjackFalse_ThreeCard21() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.SEVEN, Suit.CLUBS));
        hand.addCard(new Card(Rank.SEVEN, Suit.DIAMOND));
        hand.addCard(new Card(Rank.SEVEN, Suit.HEARTS)); // 21 but 3 cards

        assertFalse(hand.isBlackjack(), "21 with 3 cards is not blackjack");
    }

    @Test
    void testClearHand() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.TEN, Suit.SPADES));
        hand.addCard(new Card(Rank.FIVE, Suit.CLUBS));

        hand.clearHand();
        assertEquals(0, hand.getCards().size(), "Hand should be empty after clear");
        assertEquals(0, hand.getValue(), "Hand value should reset");
    }

    @Test
    void testToStringDoesNotCrash() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.SPADES));
        hand.addCard(new Card(Rank.FOUR, Suit.HEARTS));

        assertNotNull(hand.toString());
        assertTrue(hand.toString().contains("value"), "toString should show hand value");
    }
}