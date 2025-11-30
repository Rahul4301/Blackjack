package Server;

import Enums.AccState;

public class Dealer extends Account {
    private static int count;

    private String dealerID;
    private GameTable currentTable;
    private Hand hand;

    public Dealer(String username, String password) {
        this.username = username;
        this.password = password;
        dealerID = ("D" + ++count);
        hand = new Hand();
        sessionActive = false;
        accountState = AccState.ACTIVE;
    }

    public void hit(Card card) {
        if (hand != null) {
            hand.addCard(card);
        }
    }

    /**
     * Prepare a new round: clear dealer hand.
     */
    public void prepareRound() {
        hand.clearHand();
    }

    /**
     * Play dealer's turn: hit on soft 17 or less, stand on hard 17+.
     */
    public void playDealerTurn(Shoe shoe) {
        while (shouldHit()) {
            hand.addCard(shoe.dealCard());
        }
    }

    /**
     * Determine if dealer should hit.
     * Dealer hits on soft 17, stands on hard 17+.
     */
    private boolean shouldHit() {
        int value = hand.getValue();
        if (value < 17) {
            return true;  // hit on 16 or less
        }
        if (value == 17 && hand.isSoft()) {
            return true;  // hit on soft 17
        }
        return false;  // stand on hard 17+
    }

    /**
     * Compare this dealer hand with a player hand.
     * Returns: 1 if dealer wins, -1 if player wins, 0 if push.
     */
    public int compareHands(Hand playerHand) {
        int dealerValue = hand.getValue();
        int playerValue = playerHand.getValue();

        if (hand.isBust()) {
            return -1;  // dealer bust, player wins
        }
        if (playerHand.isBust()) {
            return 1;   // player bust, dealer wins
        }
        if (dealerValue > playerValue) {
            return 1;   // dealer hand higher
        }
        if (playerValue > dealerValue) {
            return -1;  // player hand higher
        }
        return 0;  // push
    }

    public Hand getHand() {
        return hand;
    }

    public int getHandValue() {
        return hand.getValue();
    }

    public String getID() {
        return dealerID;
    }

    public GameTable getTable() {
        return currentTable;
    }

    @Override
    public String toString() {
        return "Dealer[" + dealerID + ", hand=" + hand + "]";
    }
}
