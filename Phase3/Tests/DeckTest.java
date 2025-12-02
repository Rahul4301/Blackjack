package Tests;

import Server.Deck;
import Server.Card;
import Enums.Rank;
import Enums.Suit;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DeckTest {

    @Test
    public void testNewDeckHas52Cards() {
        Deck deck = new Deck();
        assertEquals(52, deck.cardsRemaining(), "New deck should start with 52 cards");
    }

    @Test
    public void testDeckContainsAllUniqueCards() {
        Deck deck = new Deck();
        Set<String> seen = new HashSet<>();

        for (int i = 0; i < 52; i++) {
            Card c = deck.dealCard();
            assertNotNull(c, "Deck should not return null before 52 cards are dealt");

            String key = c.getRank().name() + " of " + c.getSuit().name();
            assertFalse(seen.contains(key), "Deck should not contain duplicate cards: " + key);
            seen.add(key);
        }

        assertEquals(52, seen.size(), "Deck should contain 52 unique cards");
        assertEquals(0, deck.cardsRemaining(), "Deck should have 0 cards remaining after dealing 52");

        // After all cards are dealt, dealCard should return null
        assertNull(deck.dealCard(), "Dealing from an empty deck should return null");
    }

    @Test
    public void testDealCardReducesCount() {
        Deck deck = new Deck();
        int start = deck.cardsRemaining();

        Card c = deck.dealCard();
        assertNotNull(c, "First dealt card should not be null");
        assertEquals(start - 1, deck.cardsRemaining(), "cardsRemaining should decrease by 1 after dealing one card");
    }

    @Test
    public void testResetDeckRestores52Cards() {
        Deck deck = new Deck();

        // Deal some cards
        for (int i = 0; i < 10; i++) {
            assertNotNull(deck.dealCard(), "Dealt card should not be null before deck is empty");
        }

        int remainingAfterDeal = deck.cardsRemaining();
        assertTrue(remainingAfterDeal <= 42 && remainingAfterDeal >= 0,
                "After dealing 10 cards, there should be at most 42 cards left");

        // Reset deck
        deck.resetDeck();
        assertEquals(52, deck.cardsRemaining(), "resetDeck should rebuild the deck to 52 cards");
    }

    @Test
    public void testShuffleDoesNotChangeCardCountOrUniqueness() {
        Deck deck = new Deck();

        // Snapshot of cards before shuffle
        Set<String> before = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            Card c = deck.dealCard();
            before.add(c.getRank().name() + "-" + c.getSuit().name());
        }

        // Rebuild and shuffle
        deck.resetDeck();
        deck.shuffle();

        assertEquals(52, deck.cardsRemaining(), "After reset and shuffle, deck should have 52 cards");

        Set<String> after = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            Card c = deck.dealCard();
            after.add(c.getRank().name() + "-" + c.getSuit().name());
        }

        assertEquals(52, after.size(), "Shuffled deck should still contain 52 unique cards");
        assertEquals(before, after, "Reset + shuffle should still contain the same set of cards (ignoring order)");
    }

    @Test
    public void testDeckIDIsNotNullAndUnique() {
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();

        assertNotNull(deck1.getDeckID(), "Deck ID should not be null");
        assertNotNull(deck2.getDeckID(), "Second deck ID should not be null");
        assertNotEquals(deck1.getDeckID(), deck2.getDeckID(),
                "Two different decks should have different IDs");
    }

    @Test
    public void testToStringContainsDeckInfo() {
        Deck deck = new Deck();
        String s = deck.toString();

        assertNotNull(s, "toString should not return null");
        assertTrue(s.contains("Deck"), "toString should mention 'Deck'");
        assertTrue(s.contains("cards remaining"), "toString should mention cards remaining");
    }
}
