package Tests;

import Server.LoginManager;
import Server.Account;
import Server.Player;
import Server.Dealer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginManagerTest {

    @Test
    public void testCreatePlayerAndDealerAccountsAndLogin() {
        LoginManager manager = new LoginManager();

        Account player = manager.createAccount("alice", "pw1", "PLAYER");
        Account dealer = manager.createAccount("bob", "pw2", "DEALER");

        assertNotNull(player, "Player account should be created");
        assertNotNull(dealer, "Dealer account should be created");

        assertTrue(player instanceof Player, "alice should be a Player");
        assertTrue(dealer instanceof Dealer, "bob should be a Dealer");

        Account loggedInPlayer = manager.login("alice", "pw1");
        assertNotNull(loggedInPlayer, "Valid player credentials should log in");
        assertEquals("alice", loggedInPlayer.getUsername());

        Account loggedInDealer = manager.login("bob", "pw2");
        assertNotNull(loggedInDealer, "Valid dealer credentials should log in");
        assertEquals("bob", loggedInDealer.getUsername());
    }

    @Test
    public void testCreateAccountDuplicateUsernameThrows() {
        LoginManager manager = new LoginManager();

        manager.createAccount("duplicate", "pw", "PLAYER");

        // Same name, different case should still be rejected
        assertThrows(IllegalArgumentException.class,
                () -> manager.createAccount("DUPLICATE", "other", "PLAYER"),
                "Creating an account with an existing username (case-insensitive) should throw");
    }

    @Test
    public void testCreatePlayerAccountConvenienceMethod() {
        LoginManager manager = new LoginManager();

        Account account = manager.createPlayerAccount("charlie", "secret");

        assertNotNull(account, "createPlayerAccount should return a new account");
        assertTrue(account instanceof Player, "createPlayerAccount should create a Player");
        assertEquals("charlie", account.getUsername());

        Account loggedIn = manager.login("charlie", "secret");
        assertNotNull(loggedIn, "Player created via convenience method should be able to log in");
        assertEquals("charlie", loggedIn.getUsername());
    }

    @Test
    public void testLoginInvalidCredentialsReturnsNull() {
        LoginManager manager = new LoginManager();
        manager.createAccount("dave", "pw", "PLAYER");

        Account badUser = manager.login("notdave", "pw");
        Account badPassword = manager.login("dave", "wrong");

        assertNull(badUser, "Login with wrong username should return null");
        assertNull(badPassword, "Login with wrong password should return null");
    }

    @Test
    public void testToStringIncludesUserInfo() {
        LoginManager manager = new LoginManager();
        manager.createAccount("emma", "hiddenpw", "PLAYER");
        manager.createAccount("frank", "dealerpw", "DEALER");

        String s = manager.toString();

        assertNotNull(s, "toString should not return null");
        assertTrue(s.contains("emma"), "toString should contain the username for player");
        assertTrue(s.contains("hiddenpw"), "toString should contain the password for player");
        assertTrue(s.contains("frank"), "toString should contain the username for dealer");
        assertTrue(s.contains("dealerpw"), "toString should contain the password for dealer");

        assertTrue(s.contains("Player") || s.contains("player"),
                "toString should include the 'Player' type label");
        assertTrue(s.contains("Dealer") || s.contains("dealer"),
                "toString should include the 'Dealer' type label");
    }
}
