package Client;

import Shared.TableSnapshot;
import Shared.PlayerView;
import Shared.DealerView;
import Shared.CardView;
import Enums.GameState;
import Enums.Rank;
import Enums.Suit;

import javax.swing.SwingUtilities;
import java.util.Arrays;
import java.util.List;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();

            // ---- Fake dealer hand ----
            CardView dealerHidden = new CardView(Rank.TEN, Suit.HEARTS, true);   // face down
            CardView dealerUp = new CardView(Rank.SEVEN, Suit.CLUBS, false);     // face up
            DealerView dealerView = new DealerView(
                    Arrays.asList(dealerHidden, dealerUp),
                    true // hasHiddenCard
            );

            // ---- Fake player hand ----
            CardView p1 = new CardView(Rank.ACE, Suit.SPADES, false);
            CardView p2 = new CardView(Rank.JACK, Suit.DIAMONDS, false);

            PlayerView you = new PlayerView(
                    "player-1",
                    "You",
                    10.0,
                    21,
                    true,   // active
                    true,   // you
                    true,   // your turn
                    Arrays.asList(p1, p2)
            );

            List players = Arrays.asList(you);

            TableSnapshot snapshot = new TableSnapshot(
                    "T1",
                    GameState.IN_PROGRESS,
                    "player-1",
                    dealerView,
                    players
            );

            gui.displayTable(snapshot);
        });
    }
}
