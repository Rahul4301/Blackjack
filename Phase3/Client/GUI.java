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
    private Client client;

    public GUI() {
        this(null);
    }
    public GUI(Client client) {
        this.client = client;
        this.currentScreen = "LOGIN";
        initFrame();
        showLoginScreen();
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

    private PlayerView findYou(List players) {
        if (players == null) return null;

        for (Object o : players) {
            if (o instanceof PlayerView pv && pv.isYou()) {
                return pv;
            }
        }
        for (Object o : players) {
            if (o instanceof PlayerView pv) {
                return pv;
            }
        }
        return null;
    }

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

    public void updateGameState() { /* hook up to server later */ }

    public void showPlayerOptions() { /* show buttons later */ }

    public void showDealerOptions() { /* dealer controls later */ }

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

    public void showLoginScreen() {
        currentScreen = "LOGIN";
        rootPanel.removeAll();
        
        JLabel title = new JLabel("Blackjack Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.setOpaque(false);
        
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(userField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passField);
        
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        
        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            
            if (client != null && client.login(username, password)) {
                showLobby();  // Go to lobby after successful login
            } else {
                // For testing without client
                JOptionPane.showMessageDialog(frame, 
                    "Login successful (demo mode)", 
                    "Info", 
                    JOptionPane.INFORMATION_MESSAGE);
                showLobby();  // Go to lobby anyway for testing
            }
        });
        
        registerBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            
            if (client != null) {
                // Assuming "PLAYER" as default type
                if (client.register(username, password, "PLAYER")) {
                    showLobby();
                }
            } else {
                JOptionPane.showMessageDialog(frame, 
                    "Registration would happen here", 
                    "Info", 
                    JOptionPane.INFORMATION_MESSAGE);
                showLobby();  // Go to lobby for testing
            }
        });
        
        loginPanel.add(loginBtn);
        loginPanel.add(registerBtn);
        
        // Center everything
        rootPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        
        rootPanel.add(title, gbc);
        rootPanel.add(Box.createVerticalStrut(20));
        rootPanel.add(loginPanel, gbc);
        
        rootPanel.revalidate();
        rootPanel.repaint();
    }

    public void showLobby() {
        currentScreen = "LOBBY";
        rootPanel.removeAll();
        rootPanel.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Blackjack Lobby", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        // Table list (would be populated from server)
        DefaultListModel<String> tableModel = new DefaultListModel<>();
        tableModel.addElement("Table T1 - 0/7 players");
        tableModel.addElement("Table T2 - 3/7 players");
        tableModel.addElement("Table T3 - 7/7 players (Full)");
        
        JList<String> tableList = new JList<>(tableModel);
        tableList.setBackground(new Color(220, 220, 220));
        
        JButton joinBtn = new JButton("Join Selected Table");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn = new JButton("Logout");
        
        // Add action listeners
        joinBtn.addActionListener(e -> {
            int index = tableList.getSelectedIndex();
            if (index >= 0) {
                String selected = tableModel.getElementAt(index);
                // Extract table ID like "T1" from "Table T1 - 0/7 players"
                String tableId = selected.split(" ")[1];
                
                if (client != null) {
                    client.joinTable(tableId);
                    // The joinTable method should call displayTable() when successful
                } else {
                    // For testing without client, show a demo table
                    displayDemoTable();
                }
            } else {
                JOptionPane.showMessageDialog(frame, 
                    "Please select a table first", 
                    "Warning", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        refreshBtn.addActionListener(e -> {
            if (client != null) {
                client.listTables();
                // This should update the table list
            } else {
                JOptionPane.showMessageDialog(frame, 
                    "Refreshing table list...", 
                    "Info", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        logoutBtn.addActionListener(e -> {
            if (client != null) {
                client.logout();
            }
            showLoginScreen();  // Go back to login
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(joinBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(logoutBtn);
        
        rootPanel.add(title, BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(tableList), BorderLayout.CENTER);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        rootPanel.revalidate();
        rootPanel.repaint();
    }

    // Add this helper method for testing
    private void displayDemoTable() {
        // For demo, create a simple fake TableSnapshot
        // You can copy the test code from MainApp here
        // For now, just show a message
        JOptionPane.showMessageDialog(frame, 
            "Joining table (demo mode)\n\nThe game table would appear here.", 
            "Demo", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}