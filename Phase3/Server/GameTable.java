package Server;

import Enums.BetStatus;
import Enums.GameState;
import Enums.MessageType;
import Message.Message;
import java.util.ArrayList;

public class GameTable {
    private static int count;
    private static final int MAX_PLAYERS = 7;
    private static final int SHUFFLE_THRESHOLD = 78;  // reshuffle when < 78 cards left

    private String tableID;
    private Dealer dealer;
    private Server server;
    private ArrayList<Player> players;
    private ArrayList<Bet> bets;
    private Shoe shoe;
    private GameState state;
    private double minBet;
    private double maxBet;

    public GameTable(Dealer dealer, double minBet, double maxBet) {
        tableID = ("T" + ++count);
        this.dealer = dealer;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.server = null;  // Will be set by Server after creation
        players = new ArrayList<>(MAX_PLAYERS);
        shoe = new Shoe(6);  // 6-deck shoe standard
        bets = new ArrayList<>();
        state = GameState.BETTING;
    }

    /**
     * Set the server reference for broadcasting updates.
     */
    public void setServer(Server server) {
        this.server = server;
    }

    public boolean addPlayer(Player player) {
        if (players.size() < MAX_PLAYERS && player != null) {
            players.add(player);
            return true;
        }
        return false;
    }

    public void removePlayer(Player player) {
        players.remove(player);
        // Also remove associated bet
        bets.removeIf(b -> b.getPlayer().equals(player));
    }

    public void addBet(Player player, Bet bet) {
        if (bet != null) {
            bets.add(bet);
        }
    }

    /**
     * Start a new round: betting phase.
     */
    public void startRound() {
        state = GameState.BETTING;
        dealer.prepareRound();
        bets.clear();
        
        // Clear previous hands
        for (Player p : players) {
            p.clearHand();
        }

        // Check if reshuffle is needed
        if (shoe.cardsRemaining() < SHUFFLE_THRESHOLD) {
            shoe.resetShoe();
        }
        
        broadcastUpdate();  // Notify all players round has started
    }

    /**
     * Deal initial two cards to players and dealer.
     */
    public void dealInitialCards() {
        state = GameState.DEALING;

        // Deal two cards to each player
        for (Player p : players) {
            for (int i = 0; i < 2; i++) {
                p.hit(shoe.dealCard());
            }
        }

        // Deal two cards to dealer (one face up, one face down in UI)
        for (int i = 0; i < 2; i++) {
            dealer.hit(shoe.dealCard());
        }

        state = GameState.IN_PROGRESS;
        broadcastUpdate();  // Notify all players of dealt cards
    }

    /**
     * Handle player actions (hit/stand/double/split).
     * Returns true if action is valid.
     */
    public boolean processPlayerAction(Player player, String action) {
        if (!players.contains(player)) {
            return false;
        }

        boolean valid = false;
        switch (action.toLowerCase()) {
            case "hit":
                player.hit(shoe.dealCard());
                valid = true;
                break;

            case "stand":
                player.stand();
                valid = true;
                break;

            case "double":
                if (player.doubleDown()) {
                    player.hit(shoe.dealCard());  // Double down gives one more card
                    valid = true;
                }
                break;

            case "split":
                if (player.split()) {
                    // TODO: handle split logic (two hands)
                    valid = true;
                }
                break;
        }
        
        if (valid) {
            broadcastUpdate();  // Broadcast action result to all players
        }
        return valid;
    }

    /**
     * Evaluate all hands and settle bets.
     */
    public void evaluateHands() {
        // Dealer plays their turn
        dealer.playDealerTurn(shoe);

        // Evaluate each player's hand
        for (Player player : players) {
            Hand playerHand = player.getHand();
            Bet bet = player.getBet();

            if (bet == null) {
                continue;  // no bet placed
            }

            // Check for player blackjack
            if (playerHand.isBlackjack() && dealer.getHand().getValue() != 21) {
                bet.settle(BetStatus.BLACKJACK);
                continue;
            }

            // Compare hands
            int comparison = dealer.compareHands(playerHand);

            if (comparison == 1) {
                // Dealer wins
                bet.settle(BetStatus.LOSE);
            } else if (comparison == -1) {
                // Player wins
                bet.settle(BetStatus.WIN);
            } else {
                // Push
                bet.settle(BetStatus.PUSH);
            }
        }

        state = GameState.RESULTS;
        broadcastUpdate();  // Notify all players of results
    }

    /**
     * Reset table for next round.
     */
    public void resetTable() {
        // Clear hands but keep players
        for (Player p : players) {
            p.clearHand();
        }
        bets.clear();
        state = GameState.BETTING;
        broadcastUpdate();  // Notify all players table has been reset
    }

    public void broadcastUpdate() {
        if (server == null) return;
        Message update = new Message(
            MessageType.GAME_UPDATE,
            "SERVER",
            "TABLE_UPDATE",
            this,
            java.time.LocalDateTime.now()
        );
        server.broadcastToTable(this, update);
    }

    // ===== Getters =====
    public String getTableID() {
        return tableID;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public ArrayList<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public ArrayList<Bet> getBets() {
        return new ArrayList<>(bets);
    }

    public Shoe getShoe() {
        return shoe;
    }

    public GameState getState() {
        return state;
    }

    public double getMinBet() {
        return minBet;
    }

    public double getMaxBet() {
        return maxBet;
    }

    @Override
    public String toString() {
        return "GameTable[" + tableID + ", players=" + players.size() + ", state=" + state + "]";
    }
}
