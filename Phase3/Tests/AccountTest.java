package Tests;

import Server.Account;
import Server.Player;
import Enums.AccState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    void testGetUsername() {
        Account account = new Player("alice", "secret", 100.0);
        assertEquals("alice", account.getUsername(),
                "getUsername should return the username set in the constructor");
    }

    @Test
    void testInitialAccountStateIsActive() {
        Account account = new Player("bob", "pw123", 50.0);
        assertEquals(AccState.ACTIVE, account.getAccState(),
                "New Player account should start in ACTIVE state");
    }

    @Test
    void testToStringContainsUsernameAndPassword() {
        Account account = new Player("charlie", "mypw", 200.0);
        String s = account.toString();

        assertNotNull(s, "toString should not return null");
        assertTrue(s.contains("charlie"),
                "toString should contain the username");
        assertTrue(s.contains("mypw"),
                "toString should contain the password");
        assertTrue(s.startsWith("Username: "),
                "toString should start with 'Username: ' as defined in Account");
    }
}
