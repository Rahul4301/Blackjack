package Tests;

import Server.Bet;
import Server.Player;
import Enums.BetStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BetTest {

    @Test
    void testValidBetCreation() {
        Player p = new Player("alice", "pw", 100.0);
        Bet bet = new Bet(p, 20.0);

        assertEquals(20.0, bet.getAmount(), "Bet amount should be stored correctly");
        assertEquals(BetStatus.PENDING, bet.getOutcome(), "New bets should start as PENDING");
        assertFalse(bet.isSettled(), "New bets should not be settled");
        assertEquals(p, bet.getPlayer(), "Player should be associated with bet");
    }

    @Test
    void testInvalidBetThrowsException() {
        Player p = new Player("bob", "pw", 10.0);

        assertThrows(IllegalArgumentException.class,
                () -> new Bet(p, 0),
                "Bet of 0 should be invalid");

        assertThrows(IllegalArgumentException.class,
                () -> new Bet(p, -5),
                "Negative bet should be invalid");

        assertThrows(IllegalArgumentException.class,
                () -> new Bet(p, 999),
                "Bet greater than balance should be invalid");
    }

    @Test
    void testCalculatePayoutBlackjack() {
        Player p = new Player("carl", "pw", 100);
        Bet bet = new Bet(p, 20);

        bet.settle(BetStatus.BLACKJACK);
        assertEquals(BetStatus.BLACKJACK, bet.getOutcome());
        assertEquals(20 * 1.5, bet.calculatePayout(), 0.0001);
    }

    @Test
    void testCalculatePayoutWin() {
        Player p = new Player("dan", "pw", 100);
        Bet bet = new Bet(p, 30);

        bet.settle(BetStatus.WIN);
        assertEquals(30, bet.calculatePayout(), 0.0001);
    }

    @Test
    void testCalculatePayoutPush() {
        Player p = new Player("ed", "pw", 100);
        Bet bet = new Bet(p, 50);

        bet.settle(BetStatus.PUSH);
        assertEquals(0, bet.calculatePayout(), 0.0001);
    }

    @Test
    void testCalculatePayoutLose() {
        Player p = new Player("frank", "pw", 100);
        Bet bet = new Bet(p, 40);

        bet.settle(BetStatus.LOSE);
        assertEquals(-40, bet.calculatePayout(), 0.0001);
    }

    @Test
    void testSettleUpdatesPlayerBalance() {
        Player p = new Player("gina", "pw", 100);
        Bet bet = new Bet(p, 20);

        bet.settle(BetStatus.WIN);  // +20

        assertTrue(bet.isSettled(), "Bet should be marked as settled");
        assertEquals(BetStatus.WIN, bet.getOutcome(), "Outcome should be WIN");
        assertEquals(120.0, p.getBalance(), 0.0001, "Player should have updated balance");
    }

    @Test
    void testSettleLoseSubtractsBalance() {
        Player p = new Player("henry", "pw", 100);
        Bet bet = new Bet(p, 25);

        bet.settle(BetStatus.LOSE); // -25

        assertEquals(75.0, p.getBalance(), 0.0001, "Losing result should subtract from player balance");
    }

    @Test
    void testToString() {
        Player p = new Player("ivy", "pw", 100);
        Bet bet = new Bet(p, 15);

        String s = bet.toString();
        assertTrue(s.contains("ivy"), "toString should include username");
        assertTrue(s.contains("15"), "toString should include bet amount");
    }
}
