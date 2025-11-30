package Client;

import Server.GameTable;
import Server.Player;
import Server.Hand;
import Server.Dealer;
import Enums.GameState;

/**
 * Enhanced GUI for displaying Blackjack game state and animations.
 */
public class GUI {
    private GameState currentState;
    private GameTable currentTable;

    public GUI() {
        this.currentState = GameState.BETTING;
    }

    /**
     * Display the login screen.
     */
    public void displayLoginScreen() {
        printHeader("BLACKJACK - LOGIN");
        System.out.println(divider("═"));
        System.out.println("Welcome to the Casino!");
        System.out.println("Please login to your account or register a new one.");
        System.out.println(divider("═"));
    }

    /**
     * Display the game lobby showing available tables.
     */
    public void displayLobby(java.util.List<GameTable> tables) {
        printHeader("GAME LOBBY");
        System.out.println(divider("═"));
        System.out.println("Available Tables:");
        System.out.println();
        
        int tableNum = 1;
        for (GameTable table : tables) {
            System.out.printf("  %d. Table %s | Players: %d/7 | Min Bet: $10 | Max Bet: $1000%n",
                tableNum, table.getTableID(), table.getPlayers().size());
            tableNum++;
        }
        System.out.println(divider("═"));
    }

    /**
     * Display active game table with real-time state.
     */
    public void displayTable(GameTable table) {
        this.currentTable = table;
        this.currentState = table.getState();
        
        clearScreen();
        printHeader("⚡ BLACKJACK TABLE " + table.getTableID());
        
        // Display current game state
        displayGameState(table);
        
        // Display dealer section
        displayDealerSection(table);
        
        // Display player section
        displayPlayerSection(table);
        
        // Display bets
        displayBetsSection(table);
    }

    /**
     * Display current game state.
     */
    private void displayGameState(GameTable table) {
        System.out.println();
        String stateMessage = switch (table.getState()) {
            case BETTING -> "🎲 BETTING PHASE - Place your bets!";
            case DEALING -> "🃏 Dealing cards...";
            case IN_PROGRESS -> "🎯 IN PROGRESS - Make your move!";
            case RESULTS -> "📊 RESULTS - Round ended!";
        };
        System.out.println("  " + stateMessage);
        System.out.println(divider("─"));
    }

    /**
     * Display dealer section.
     */
    private void displayDealerSection(GameTable table) {
        System.out.println("\n  📍 DEALER:");
        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.println("  │ Hand: [?] [?] = ?                   │");
        System.out.println("  └─────────────────────────────────────┘");
    }

    /**
     * Display player section with all players at table.
     */
    private void displayPlayerSection(GameTable table) {
        System.out.println("\n  👥 PLAYERS:");
        int playerNum = 1;
        for (Player player : table.getPlayers()) {
            String handDisplay = formatHandDisplay(player.getHand());
            System.out.printf("  %d. %-15s | Hand: %s | Balance: $%-7.0f%n",
                playerNum, player.getUsername(), handDisplay, player.getBalance());
            playerNum++;
        }
    }

    /**
     * Display active bets.
     */
    private void displayBetsSection(GameTable table) {
        System.out.println("\n  💰 BETS:");
        if (table.getBets().isEmpty()) {
            System.out.println("     No bets placed yet");
        } else {
            System.out.printf("     Active bets: %d%n", table.getBets().size());
        }
    }

    /**
     * Show player action options.
     */
    public void showPlayerOptions() {
        System.out.println("\n  🎮 YOUR OPTIONS:");
        System.out.println("     1) Hit      - Take another card");
        System.out.println("     2) Stand    - End your turn");
        System.out.println("     3) Double   - Double your bet and take one card");
        System.out.println("     4) Split    - Split your hand (if possible)");
        System.out.println("     5) Bet      - Place a new bet");
        System.out.println("     6) Leave    - Leave the table");
    }

    /**
     * Show animation for card dealing.
     */
    public void animateDeal() {
        System.out.println("\n  [Dealing cards...]");
        printAnimation();
    }

    /**
     * Show animation for player hit.
     */
    public void animateHit() {
        System.out.println("\n  [Card coming...]");
        printAnimation();
    }

    /**
     * Show round results animation.
     */
    public void animateResults() {
        System.out.println("\n  [Calculating results...]");
        printAnimation();
    }

    /**
     * Display error message.
     */
    public void displayError(String message) {
        System.out.println("\n  ❌ ERROR: " + message);
    }

    /**
     * Display success message.
     */
    public void displaySuccess(String message) {
        System.out.println("\n  ✓ " + message);
    }

    /**
     * Display information message.
     */
    public void displayInfo(String message) {
        System.out.println("\n  ℹ️ " + message);
    }

    /**
     * Format hand display string.
     */
    private String formatHandDisplay(Hand hand) {
        if (hand == null || hand.getCards().size() == 0) {
            return "[·]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < hand.getCards().size(); i++) {
            sb.append("🂠");  // Card back
            if (i < hand.getCards().size() - 1) sb.append(" ");
        }
        sb.append("] = ").append(hand.getValue());
        return sb.toString();
    }

    /**
     * Helper: Print header.
     */
    private void printHeader(String title) {
        System.out.println();
        System.out.println(divider("═"));
        System.out.println("  " + title);
        System.out.println(divider("═"));
    }

    /**
     * Helper: Print divider.
     */
    private String divider(String character) {
        return character.repeat(40);
    }

    /**
     * Helper: Print loading animation.
     */
    private void printAnimation() {
        for (int i = 0; i < 3; i++) {
            System.out.print(".");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();
    }

    /**
     * Helper: Clear screen (approximate).
     */
    private void clearScreen() {
        for (int i = 0; i < 5; i++) {
            System.out.println();
        }
    }
}
