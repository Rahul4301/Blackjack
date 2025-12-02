package Tests;

import Server.Player;
import Server.Card;
import Server.Bet;
import Server.Hand;
import Enums.Rank;
import Enums.Suit;
import Enums.AccState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testInitialState() {
        Player p = new Player("alice", "pw", 500.0);

        assertEquals("alice", p.getUsername(), "Username should be set from constructor");
        assertEquals(500.0, p.getBalance(), 0.0001, "Balance should match constructor value");
        assertNotNull(p.getHand(), "Hand should be initialized");
        assertEquals(0, p.getHandValue(), "New hand should have value 0");
        assertFalse(p.isBust(), "New player should not be bust");
        assertNull(p.getBet(), "No active bet on new player");
        assertTrue(p.getID().startsWith("P"), "ID should start with 'P'");
        assertEquals(AccState.ACTIVE, p.getAccState(), "New player should start in ACTIVE state");
    }

    @Test
    public void testPlaceBetCreatesBetAndAssociatesWithPlayer() {
        Player p = new Player("bob", "pw", 300.0);

        Bet bet = p.placeBet(50.0);

        assertNotNull(bet, "placeBet should return a Bet");
        assertEquals(50.0, bet.getAmount(), 0.0001, "Bet amount should match requested amount");
        assertSame(p, bet.getPlayer(), "Bet should reference the player who placed it");
        assertSame(bet, p.getBet(), "Player's active bet should be the one just placed");
    }

    @Test
    public void testPlaceBetInvalidAmountThrows() {
        Player p = new Player("carol", "pw", 100.0);

        assertThrows(IllegalArgumentException.class,
                () -> p.placeBet(0),
                "Bet of 0 should be invalid");

        assertThrows(IllegalArgumentException.class,
                () -> p.placeBet(-10),
                "Negative bet should be invalid");

        assertThrows(IllegalArgumentException.class,
                () -> p.placeBet(200),
                "Bet larger than balance should be invalid");
    }

    @Test
    public void testHitNoBust() {
        Player p = new Player("dave", "pw", 200.0);

        boolean bust = p.hit(new Card(Rank.FIVE, Suit.CLUBS)); // 5
        assertFalse(bust, "Hit with total <= 21 should not bust");
        assertEquals(5, p.getHandValue(), "Hand value should be 5 after hitting with a FIVE");
        assertFalse(p.isBust(), "Player should not be bust at 5");
    }

    @Test
    public void testHitBustAfterMultipleCards() {
        Player p = new Player("erin", "pw", 200.0);

        assertFalse(p.hit(new Card(Rank.TEN, Suit.HEARTS)));   // 10
        assertFalse(p.hit(new Card(Rank.KING, Suit.SPADES)));  // 20
        boolean bust = p.hit(new Card(Rank.TWO, Suit.DIAMONDS)); // 22

        assertTrue(bust, "Hitting to a total over 21 should return true (bust)");
        assertTrue(p.isBust(), "isBust should be true when hand > 21");
        assertTrue(p.getHandValue() > 21, "Hand value should be over 21 when bust");
    }

    @Test
    public void testUpdateBalancePositiveAndNegative() {
        Player p = new Player("frank", "pw", 100.0);

        p.updateBalance(50.0);   // 150
        assertEquals(150.0, p.getBalance(), 0.0001, "Balance should increase by 50");

        p.updateBalance(-20.0);  // 130
        assertEquals(130.0, p.getBalance(), 0.0001, "Balance should decrease by 20");
    }

    @Test
    public void testGetHandAndGetHandValueConsistent() {
        Player p = new Player("gina", "pw", 100.0);

        Hand hand = p.getHand();
        assertNotNull(hand, "getHand should never return null");

        p.hit(new Card(Rank.ACE, Suit.SPADES));  // 11
        p.hit(new Card(Rank.SIX, Suit.HEARTS));  // 17 (Ace + 6)

        assertEquals(hand.getValue(), p.getHandValue(),
                "getHandValue should reflect the same total as the underlying Hand");
    }

    @Test
    public void testPlayerIDsAreUnique() {
        Player p1 = new Player("henry", "x", 50.0);
        Player p2 = new Player("irene", "y", 60.0);

        assertNotNull(p1.getID(), "First player ID should not be null");
        assertNotNull(p2.getID(), "Second player ID should not be null");
        assertNotEquals(p1.getID(), p2.getID(),
                "Two different players should have different IDs");
    }
}
