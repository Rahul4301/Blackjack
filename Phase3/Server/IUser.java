package Server;

import Enums.AccState;

/**
 * Common behavior for user-like accounts (Player, Dealer, etc.).
 */
public interface IUser {

    String getUsername();

    AccState getAccState();

    /**
     * Whether this user's session is currently active.
     */
    boolean isSessionActive();
}