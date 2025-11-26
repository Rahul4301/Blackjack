package Server;

import Enums.AccState;

/**
 * Abstract base class for all accounts (Player, Dealer).
 */
public abstract class Account {
    protected String username;
    protected String password;
    protected boolean sessionActive;
    protected AccState accountState;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        this.sessionActive = false;
        this.accountState = AccState.ACTIVE;
    }

    /**
     * Login logic (sets sessionActive to true).
     * Subclasses may override for role-specific behavior.
     */
    public boolean login() {
        if (accountState == AccState.ACTIVE) {
            this.sessionActive = true;
            return true;
        }
        return false;
    }

    /**
     * Logout logic (sets sessionActive to false).
     */
    public void logout() {
        this.sessionActive = false;
    }

    // ===== Getters =====
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSessionActive() {
        return sessionActive;
    }

    public AccState getAccountState() {
        return accountState;
    }

    // ===== Setters =====
    public void setSessionActive(boolean sessionActive) {
        this.sessionActive = sessionActive;
    }

    public void setAccountState(AccState accountState) {
        this.accountState = accountState;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[username=" + username + ", sessionActive=" + sessionActive + ", accountState=" + accountState + "]";
    }
}
