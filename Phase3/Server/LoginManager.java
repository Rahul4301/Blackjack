package Server;

import Enums.AccState;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages authentication and session lifecycle for accounts.
 */
public class LoginManager {
    private Map<String, Account> activeSessions;

    public LoginManager() {
        this.activeSessions = new HashMap<>();
    }

    /**
     * Authenticates a user by username and password.
     * Returns the Account if credentials are valid and account is ACTIVE.
     * Returns null if authentication fails or account is not ACTIVE.
     */
    public Account authenticate(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return null;
        }

        // TODO: Query database or user store for account with this username
        // For now, return null (placeholder for database integration)
        Account account = getUserFromDatabase(username);

        if (account == null) {
            return null; // User not found
        }

        if (!account.getPassword().equals(password)) {
            return null; // Password mismatch
        }

        if (account.getAccountState() != AccState.ACTIVE) {
            return null; // Account is suspended or locked
        }

        // Set session active and track it
        account.setSessionActive(true);
        activeSessions.put(username, account);

        return account;
    }

    /**
     * Logs out an account by clearing its session.
     */
    public void logout(Account account) {
        if (account != null) {
            account.setSessionActive(false);
            activeSessions.remove(account.getUsername());
        }
    }

    /**
     * Placeholder for database lookup. Replace with actual DB query.
     */
    private Account getUserFromDatabase(String username) {
        // TODO: Implement database lookup
        return null;
    }

    /**
     * Check if an account has an active session.
     */
    public boolean isSessionActive(String username) {
        return activeSessions.containsKey(username);
    }

    /**
     * Get active sessions count (for monitoring).
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
