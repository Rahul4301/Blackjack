package Tests;

import Enums.GameState;
import Enums.PlayerAction;
import Shared.TableSnapshot;
import Shared.PlayerView;
import Shared.DealerView;
import Shared.CardView;
import Server.*;

public class GameTableTest {

    public static void main(String[] args) {
        // IMPORTANT: for this test to work safely, see notes below about:
        // - Player hand initialization
        // - Dealer hand initialization
        // - Hand.getValue logic
        // - Dealer.mustHit logic

        // Create dealer and table
        Dealer dealer = new Dealer("dealer", "pw");
        // You should ensure Dealer creates a Hand in its constructor, for example:
        //   hand = new Hand();
        // If that is not done, this will null pointer in DealInitialCards.

        GameTable table = new GameTable(dealer);

        // Create two players
        Player p1 = new Player("alice", "pw", 100.0);
        Player p2 = new Player("bob", "pw", 100.0);

        // Same story as Dealer: Player should create a Hand in its constructor, for example:
        //   hand = new Hand();
        // Otherwise any call to p.hit(...) will null pointer.

        // Add players to table
        table.addPlayer(p1);
        table.addPlayer(p2);

        // Place simple bets
        p1.placeBet(10.0);
        p2.placeBet(15.0);

        System.out.println("Created table " + table.getTableID());
        System.out.println("Starting round");

        // Start round (deals initial cards and enters IN_PROGRESS)
        table.startRound();

        // Show initial snapshot from p1's perspective
        TableSnapshot snap = table.createSnapshotFor(p1.getID());
        printSnapshot("After startRound", snap);

        // Simple auto-play loop:
        // While the table is IN_PROGRESS, look at whose turn it is and decide:
        // - HIT if hand value < 17
        // - STAND otherwise
        while (snap.getState() == GameState.IN_PROGRESS) {

            String currentId = snap.getCurrentPlayerUsername();
            if (currentId == null) {
                System.out.println("No current player id while IN_PROGRESS, something is wrong.");
                break;
            }

            Player current = null;
            for (Player p : table.getPlayers()) {
                if (p.getID().equalsIgnoreCase(currentId)) {
                    current = p;
                    break;
                }
            }

            if (current == null) {
                System.out.println("Could not find current player " + currentId);
                break;
            }

            int value = current.getHandValue();
            PlayerAction action = (value < 17) ? PlayerAction.HIT : PlayerAction.STAND;

            System.out.println();
            System.out.println("Current player: " + current.getUsername()
                               + " (hand value " + value + ") chooses " + action);

            boolean ok = table.handlePlayerAction(current.getID(), action);
            if (!ok) {
                System.out.println("handlePlayerAction returned false for " + current.getUsername());
                break;
            }

            // Rebuild snapshot for p1 after state change
            snap = table.createSnapshotFor(p1.getID());
            printSnapshot("After " + current.getUsername() + " " + action, snap);
        }

        System.out.println();
        System.out.println("Round finished. Final snapshot from Alice's perspective:");
        snap = table.createSnapshotFor(p1.getID());
        printSnapshot("Final", snap);

        // Show final balances and bet outcomes
        System.out.println();
        System.out.println("Final balances and outcomes:");
        for (Player p : table.getPlayers()) {
            System.out.print("Player " + p.getUsername() + " balance: " + p.getBalance());
            if (p.getBet() != null && p.getBet().isSettled()) {
                System.out.print(" (bet outcome: " + p.getBet().getOutcome() + ")");
            }
            System.out.println();
        }
    }

    private static void printSnapshot(String label, TableSnapshot snap) {
        System.out.println();
        System.out.println("=== " + label + " ===");
        System.out.println("State: " + snap.getState());
        System.out.println("Current player id: " + snap.getCurrentPlayerUsername());

        DealerView dv = snap.getDealer();
        System.out.print("Dealer cards: ");
        for (CardView cv : dv.getCards()) {
            if (cv.isHidden()) {
                System.out.print("[HIDDEN] ");
            } else {
                System.out.print(cv.getRank() + " of " + cv.getSuit() + " ");
            }
        }
        if (dv.hasHiddenCard()) {
            System.out.print("(has hidden card)");
        }
        System.out.println();

        for (PlayerView pv : snap.getPlayers()) {
            System.out.print("Player " + pv.getUsername() + " (id " + pv.getPlayerUsername() + ")");
            if (pv.isYourTurn()) {
                System.out.print(" <-- requesting player");
            }
            System.out.println();

            System.out.print("  Hand: ");
            for (CardView cv : pv.getCards()) {
                System.out.print(cv.getRank() + " of " + cv.getSuit() + " ");
            }
            System.out.println();

            System.out.println("  Bet: " + pv.getBetAmount()
                               + " | handValue: " + pv.getHandValue()
                               + " | active: " + pv.isActive());
        }
    }
}
