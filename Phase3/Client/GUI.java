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

public class GUI {

    private JFrame frame;
    private JPanel rootPanel;
    private String currentScreen;

    public GUI() {
        this.currentScreen = "TABLE";
        initFrame();
    }

    /** Window setup */
    private void initFrame() {
        frame = new JFrame("Blackjack Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null); // center on screen

        rootPanel = new JPanel();
        rootPanel.setBackground(new Color(0, 120, 0)); // casino green
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

        frame.setContentPane(rootPanel);
        frame.setVisible(true);
    }

    /** Public API: show a table snapshot (dealer + player hands) */
    public void displayTable(TableSnapshot snapshot) {
        currentScreen = "TABLE";

        rootPanel.removeAll();

        // ---- Dealer row ----
        JLabel dealerLabel = new JLabel("Dealer");
        dealerLabel.setForeground(Color.WHITE);
        dealerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dealerLabel.setFont(dealerLabel.getFont().deriveFont(Font.BOLD, 22f));

        DealerView dealerView = snapshot.getDealer();
        JPanel dealerCardsPanel = buildCardsPanel(
                dealerView != null ? dealerView.getCards() : List.of(),
                true, // dealer row (respect hidden)
                dealerView != null && dealerView.hasHiddenCard()
        );

        // ---- Player row (we try to find "you") ----
        PlayerView you = findYou(snapshot.getPlayers());
        String playerTitle = (you != null ? "You: " + you.getUsername() : "Player");
        JLabel playerLabel = new JLabel(playerTitle);
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerLabel.setFont(playerLabel.getFont().deriveFont(Font.BOLD, 22f));

        JPanel playerCardsPanel = buildCardsPanel(
                you != null ? you.getCards() : List.of(),
                false,
                false
        );

        // ---- Layout ----
        rootPanel.add(Box.createVerticalStrut(30));
        rootPanel.add(dealerLabel);
        rootPanel.add(Box.createVerticalStrut(10));
        rootPanel.add(dealerCardsPanel);
        rootPanel.add(Box.createVerticalStrut(40));
        rootPanel.add(playerLabel);
        rootPanel.add(Box.createVerticalStrut(10));
        rootPanel.add(playerCardsPanel);
        rootPanel.add(Box.createVerticalGlue());

        rootPanel.revalidate();
        rootPanel.repaint();
    }

    /** Build a horizontal panel of card labels */
    private JPanel buildCardsPanel(List cards, boolean dealerRow, boolean dealerHasHidden) {
        JPanel panel = new JPanel();
        panel.setOpaque(false); // let green background show
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

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
            text = "??";
        } else {
            text = rankToString(card.getRank()) + suitToSymbol(card.getSuit());
        }

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        label.setPreferredSize(new Dimension(60, 90));

        return label;
    }

    /** Try to find the PlayerView that represents "you" */
    private PlayerView findYou(List players) {
        if (players == null) return null;

        // 1) prefer the one marked as "you"
        for (Object o : players) {
            if (o instanceof PlayerView pv && pv.isYou()) {
                return pv;
            }
        }
        // 2) otherwise just return first one, if any
        for (Object o : players) {
            if (o instanceof PlayerView pv) {
                return pv;
            }
        }
        return null;
    }

    // Helpers to make nice text for ranks/suits
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

    private String suitToSymbol(Suit suit) {
        if (suit == null) return "?";
        return switch (suit) {
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
            case SPADES -> "♠";
        };
    }

    // You can keep or expand these later if you want
    public void displayLoginScreen() {
        currentScreen = "LOGIN";
        JOptionPane.showMessageDialog(frame, "Login screen (not implemented yet)");
    }

    public void displayLobby() {
        currentScreen = "LOBBY";
        JOptionPane.showMessageDialog(frame, "Lobby screen (not implemented yet)");
    }

    public void updateGameState() { /* hook up to server later */ }

    public void showPlayerOptions() { /* show buttons later */ }

    public void showDealerOptions() { /* dealer controls later */ }

    public void showAnimations() { /* animations later */ }

    public void displayError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void displayResult() { /* result popup later */ }

    public boolean askPlayAgain() { return false; }

    public void shutdown() {
        if (frame != null) {
            frame.dispose();
        }
        System.out.println("Shutting down GUI");
    }
}
