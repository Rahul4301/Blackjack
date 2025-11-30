package Server;

import Enums.AccState;

public class Player extends Account {
    private static int count;

    private String ID;
    private double balance;
    private String sessionID;
    private Hand hand;
    private Bet activeBet;

    public Player(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.ID = ("P" + ++count);
        sessionID = null;
        hand = new Hand();
        accountState = AccState.ACTIVE;
        this.activeBet = null;
    }

    /**
     * Place a bet. Returns the Bet object if valid, null otherwise.
     */
    public Bet placeBet(double amt) {
        if (amt > 0 && amt <= balance) {
            activeBet = new Bet(this, amt);
            balance -= amt;  // Deduct bet amount from balance
            return activeBet;
        }
        return null;
    }

    /**
     * Add a card to the player's hand.
     */
    public void hit(Card card) {
        if (hand != null) {
            hand.addCard(card);
        }
    }

    /**
     * Player stands (no more cards).
     */
    public void stand() {
        // Player indicates they won't take more cards
    }

    /**
     * Double down: double the bet and receive one more card.
     * Returns true if successful, false if not allowed.
     */
    public boolean doubleDown() {
        if (hand.getCardCount() != 2 || activeBet == null) {
            return false;  // Can only double on initial 2 cards
        }
        // Double the bet and deduct from balance
        double doubleBetAmount = activeBet.getAmount();
        if (doubleBetAmount <= balance) {
            balance -= doubleBetAmount;
            activeBet = new Bet(this, activeBet.getAmount() + doubleBetAmount);
            return true;
        }
        return false;
    }

    /**
     * Split: split initial two cards into two hands.
     * Returns true if split is successful, false otherwise.
     */
    public boolean split() {
        if (!hand.canSplit() || activeBet == null) {
            return false;  // Can only split if two cards are of same rank
        }
        // TODO: Implement split logic (create second hand)
        // For now, return true to indicate split is possible
        return true;
    }

    /**
     * Update player balance (used by bet settlement).
     * This is called when a bet is settled to add winnings back.
     */
    public void updateBalance(double amount) {
        this.balance += amount;
    }

    /**
     * Clear hand for next round.
     */
    public void clearHand() {
        hand.clearHand();
        activeBet = null;
    }

    /**
     * Check if player's hand is bust.
     */
    public boolean isBust() {
        return hand.getValue() > 21;
    }

    /**
     * Get player's current hand value.
     */
    public int getHandValue() {
        return hand.getValue();
    }

    /**
     * Get player's hand object.
     */
    public Hand getHand() {
        return hand;
    }

    /**
     * Get player's active bet.
     */
    public Bet getBet() {
        return activeBet;
    }

    public double getBalance() {
        return balance;
    }

    public String getUsername() {
        return username;
    }

    public String getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "Player[" + ID + ", " + username + ", balance=" + balance + "]";
    }
}
