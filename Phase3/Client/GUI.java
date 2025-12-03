package Client;

import Enums.Rank;
import Enums.Suit;
import Shared.CardView;
import Shared.DealerView;
import Shared.PlayerView;
import Shared.TableSnapshot;
import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * GUI for the Blackjack client. Displays game tables, hands, and manages screen transitions.
 * Features a casino-themed green felt background with professional card rendering.
 */
public class GUI {

    private final JFrame frame;
    private final JPanel rootPanel;
    private String currentScreen;
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;
    private static final Color CASINO_GREEN = new Color(0, 120, 0);
    private static final Color CARD_TEXT = new Color(200, 20, 20);

    public GUI() {
        this.currentScreen = "TABLE";
        this.frame = new JFrame("Blackjack Client - Professional Edition");
        this.rootPanel = new JPanel();
        initFrame();
    }

    /** Window setup and initialization */
    private void initFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        rootPanel.setBackground(CASINO_GREEN);
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

        frame.setContentPane(rootPanel);
        frame.setVisible(true);
    }

    /** Public API: show a table snapshot (dealer + player hands) */
    public void displayTable(TableSnapshot snapshot) {
        if (snapshot == null) {
            displayError("Table snapshot is null");
            return;
        }

        currentScreen = "TABLE";
        rootPanel.removeAll();

        // ---- Title ----
        JLabel titleLabel = new JLabel("BLACKJACK TABLE");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 28f));

        // ---- Dealer row ----
        JLabel dealerLabel = new JLabel("DEALER");
        dealerLabel.setForeground(Color.WHITE);
        dealerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dealerLabel.setFont(dealerLabel.getFont().deriveFont(Font.BOLD, 20f));

        DealerView dealerView = snapshot.getDealerView();
        JPanel dealerCardsPanel = buildCardsPanel(
                dealerView != null ? dealerView.getCards() : List.of(),
                true,
                dealerView != null && dealerView.hasHiddenCard()
        );

        // ---- Player row ----
        PlayerView you = findYou(snapshot.getPlayers());
        String playerTitle = (you != null ? "YOU: " + you.getUsername() : "YOUR HAND");
        JLabel playerLabel = new JLabel(playerTitle);
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerLabel.setFont(playerLabel.getFont().deriveFont(Font.BOLD, 20f));

        JPanel playerCardsPanel = buildCardsPanel(
                you != null ? you.getCards() : List.of(),
                false,
                false
        );

        // ---- Game state info ----
        JLabel stateLabel = new JLabel("Status: " + snapshot.getState());
        stateLabel.setForeground(new Color(255, 255, 100));
        stateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stateLabel.setFont(stateLabel.getFont().deriveFont(Font.PLAIN, 14f));

        // ---- Layout ----
        rootPanel.add(Box.createVerticalStrut(20));
        rootPanel.add(titleLabel);
        rootPanel.add(Box.createVerticalStrut(20));
        rootPanel.add(dealerLabel);
        rootPanel.add(Box.createVerticalStrut(10));
        rootPanel.add(dealerCardsPanel);
        rootPanel.add(Box.createVerticalStrut(30));
        rootPanel.add(playerLabel);
        rootPanel.add(Box.createVerticalStrut(10));
        rootPanel.add(playerCardsPanel);
        rootPanel.add(Box.createVerticalStrut(20));
        rootPanel.add(stateLabel);
        rootPanel.add(Box.createVerticalGlue());

        rootPanel.revalidate();
        rootPanel.repaint();
    }

    /** Build a horizontal panel of card labels */
    private JPanel buildCardsPanel(List<?> cards, boolean dealerRow, boolean dealerHasHidden) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        int index = 0;
        for (Object o : cards) {
            if (!(o instanceof CardView card)) {
                index++;
                continue;
            }

            boolean showAsHidden = dealerRow && dealerHasHidden && card.isHidden();
            JLabel label = createCardLabel(card, showAsHidden);
            panel.add(label);
            index++;
        }

        return panel;
    }

    /** Visual representation of a single card */
    private JLabel createCardLabel(CardView card, boolean hidden) {
        String text;
        if (hidden) {
            text = "?";
        } else {
            text = rankToString(card.getRank()) + "\n" + suitToSymbol(card.getSuit());
        }

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createLineBorder(CARD_TEXT, 2));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 20f));
        label.setPreferredSize(new Dimension(70, 100));
        label.setForeground(CARD_TEXT);

        return label;
    }

    /** Try to find the PlayerView that represents "you" */
    private PlayerView findYou(List<?> players) {
        if (players == null) return null;

        // Prefer the one marked as "you"
        for (Object o : players) {
            if (o instanceof PlayerView pv && pv.isYou()) {
                return pv;
            }
        }

        // Otherwise return first one
        for (Object o : players) {
            if (o instanceof PlayerView pv) {
                return pv;
            }
        }
        return null;
    }

    /** Convert Rank enum to display string */
    private String rankToString(Rank rank) {
        if (rank == null) return "?";
        return switch (rank) {
            case TWO -> "2";
            case THREE -> "3";
            case FOUR -> "4";
            case FIVE -> "5";
            case SIX -> "6";
            case SEVEN -> "7";
            case EIGHT -> "8";
            case NINE -> "9";
            case TEN -> "10";
            case JACK -> "J";
            case QUEEN -> "Q";
            case KING -> "K";
            case ACE -> "A";
        };
    }

    /** Convert Suit enum to Unicode symbol */
    private String suitToSymbol(Suit suit) {
        if (suit == null) return "?";
        return switch (suit) {
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
            case SPADES -> "♠";
        };
    }

    /** Display a login/menu screen */
    public void displayLoginScreen() {
        currentScreen = "LOGIN";
        JOptionPane.showMessageDialog(frame, "Login screen - Connect to server", "Login", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Display game lobby with table list */
    public void displayLobby() {
        currentScreen = "LOBBY";
        JOptionPane.showMessageDialog(frame, "Lobby screen - Available tables listed here", "Lobby", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Update game state (called when server sends new snapshot) */
    public void updateGameState() {
        // This will be called from Client when new snapshots arrive
    }

    /** Show action buttons for player turn */
    public void showPlayerOptions() {
        JOptionPane.showMessageDialog(frame, "Player options: Hit, Stand, Double", "Your Turn", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Show dealer-specific options */
    public void showDealerOptions() {
        JOptionPane.showMessageDialog(frame, "Dealer options: Deal, Shuffle", "Dealer Menu", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Display error message to user */
    public void displayError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Display game result (win/loss/push) */
    public void displayResult() {
        JOptionPane.showMessageDialog(frame, "Hand result", "Round Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Ask if player wants to play another hand */
    public boolean askPlayAgain() {
        int result = JOptionPane.showConfirmDialog(frame, "Play again?", "Next Hand", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    /** Gracefully shutdown the GUI */
    public void shutdown() {
        if (frame != null) {
            frame.dispose();
        }
        System.out.println("[GUI] Shutting down GUI");
    }

    /** Check if GUI frame is still active */
    public boolean isActive() {
        return frame != null && frame.isDisplayable();
    }

    /** Get current screen name */
    public String getCurrentScreen() {
        return currentScreen;
    }
}
