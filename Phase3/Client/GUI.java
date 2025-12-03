package Client;

import Enums.PlayerAction;
import Enums.Rank;
import Enums.Suit;
import Enums.GameState;

import Shared.CardView;
import Shared.DealerView;
import Shared.PlayerView;
import Shared.TableSnapshot;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class GUI {
    private DefaultListModel<String> tableModel;
    private JList<String> tableList;
    private boolean inTableMode = false;
    private GameState lastState = null;
    private Double balanceBeforeRound = null;
    


    private javax.swing.Timer autoRefreshTimer;

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
        tableModel = new DefaultListModel<>();
        tableList = new JList<>(tableModel);
        initFrame();
        startAutoRefresh();
        showLoginScreen();
    }

    /** Attach a client instance to this GUI so the view can adapt for dealers */
    public void setClient(Client client) {
        this.client = client;
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
    //show a table snapshot (dealer + player hands)
    public void displayTable(TableSnapshot snapshot) {
        currentScreen = "TABLE";
        inTableMode = true;
        rootPanel.removeAll();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

        // Decide layout based on whether this client is a dealer
        boolean localIsDealer = (client != null && client.isDealer());

        DealerView dealerView = snapshot.getDealerView();
        JPanel dealerCardsPanel = buildCardsPanel(
            dealerView != null ? dealerView.getCards() : List.of(),
            true,
            dealerView != null && dealerView.hasHiddenCard()
        );

        // If the local user is the dealer, show players on top and dealer on bottom
        if (localIsDealer) {

    // If dealer just entered RESULTS, ask server to reset to BETTING
            if (snapshot.getState() == GameState.RESULTS
                    && lastState != GameState.RESULTS
                    && client != null) {

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TableSnapshot updated = client.requestNextRound();
                        if (updated != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    displayTable(updated);
                                }
                            });
                        }
                    }
                });
                t.start();
            }

            // Build a vertical list of each player's name + their cards
            JPanel playersContainer = new JPanel();
            playersContainer.setOpaque(false);
            playersContainer.setLayout(new BoxLayout(playersContainer, BoxLayout.Y_AXIS));

            List players = snapshot.getPlayers();
            if (players != null) {
                for (Object o : players) {
                    if (!(o instanceof PlayerView pv)) {
                        continue;
                    }
                    JLabel name = new JLabel(pv.getUsername());
                    name.setForeground(Color.WHITE);
                    name.setAlignmentX(Component.CENTER_ALIGNMENT);
                    name.setFont(name.getFont().deriveFont(Font.BOLD, 18f));

                    JPanel cardsPanel = buildCardsPanel(pv.getCards(), false, false);

                    playersContainer.add(name);
                    playersContainer.add(Box.createVerticalStrut(5));
                    playersContainer.add(cardsPanel);
                    playersContainer.add(Box.createVerticalStrut(10));
                }
            }

            // Layout: players (top) then dealer (bottom)
            rootPanel.add(Box.createVerticalStrut(20));
            rootPanel.add(playersContainer);
            rootPanel.add(Box.createVerticalStrut(30));

            JLabel dealerLabel = new JLabel("Dealer (You)");
            dealerLabel.setForeground(Color.YELLOW);
            dealerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            dealerLabel.setFont(dealerLabel.getFont().deriveFont(Font.BOLD, 22f));

            rootPanel.add(dealerLabel);
            rootPanel.add(Box.createVerticalStrut(10));
            rootPanel.add(dealerCardsPanel);
        } else {
            // ---- Dealer row ----
            JLabel dealerLabel = new JLabel("Dealer");
            dealerLabel.setForeground(Color.WHITE);
            dealerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            dealerLabel.setFont(dealerLabel.getFont().deriveFont(Font.BOLD, 22f));

            // ---- Player row (for non-dealer clients we show "you") ----
            PlayerView you = findYou(snapshot.getPlayers());

            //WIN - LOSE / BALANCE / NEXT-ROUND BLOCK
            // >>> HERE is where the win/lose / balance / next-round block goes <<<

            // Track balance at the start of betting
            if (snapshot.getState() == GameState.BETTING && you != null) {
                if (balanceBeforeRound == null) {
                    balanceBeforeRound = you.getBalance();
                }
            }

            // When we first enter RESULTS, show win / lose and restart round
            if (snapshot.getState() == GameState.RESULTS
                    && lastState != GameState.RESULTS) {

                double after = you.getBalance();
                String message;

                if (balanceBeforeRound != null) {
                    if (after > balanceBeforeRound) {
                        message = "You WIN!\n"
                                + "Before: " + balanceBeforeRound + "\n"
                                + "After: " + after;
                    } else if (after < balanceBeforeRound) {
                        message = "You LOSE.\n"
                                + "Before: " + balanceBeforeRound + "\n"
                                + "After: " + after;
                    } else {
                        message = "Push. No net change.\n"
                                + "Balance: " + after;
                    }
                } else {
                    message = "Round complete.\nYour balance: " + after;
                }

                // >>> NEW: append dealer and player hand summaries <<<
                dealerView = snapshot.getDealerView();
                String dealerLine = buildHandSummary(
                        "Dealer",
                        dealerView != null ? dealerView.getCards() : null
                );
                String playerLine = buildHandSummary(
                        "You",
                        you.getCards()
                );

                if (dealerLine != null || playerLine != null) {
                    StringBuilder sb = new StringBuilder(message);
                    sb.append("\n\n");
                    if (dealerLine != null) {
                        sb.append(dealerLine).append("\n");
                    }
                    if (playerLine != null) {
                        sb.append(playerLine);
                    }
                    message = sb.toString();
                }
                // <<< END NEW >>>

                JOptionPane.showMessageDialog(
                        frame,
                        message,
                        "Round Over",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Prepare for next round
                balanceBeforeRound = after;
            }


            // Update lastState so we only trigger once per RESULTS entry
            lastState = snapshot.getState();


            String playerTitle;
            if (you != null) {
                playerTitle = "You: " + you.getUsername();
            } else {
                playerTitle = "Player";
            }
            JLabel playerLabel = new JLabel(playerTitle);
            playerLabel.setForeground(Color.WHITE);
            playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            playerLabel.setFont(playerLabel.getFont().deriveFont(Font.BOLD, 22f));

            List cardsToShow;
            if (you != null) {
                cardsToShow = you.getCards();
            } else {
                cardsToShow = List.of();
            }

            JPanel playerCardsPanel = buildCardsPanel(
                cardsToShow,
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
        }
        
        lastState = snapshot.getState();

        // ======================================================================
        // >>> LEAVE TABLE + BACK TO LOBBY <<<
        // ======================================================================
        JPanel controlRow = new JPanel();
        controlRow.setOpaque(false);

        JButton leaveBtn = new JButton("Leave Table");
        leaveBtn.addActionListener(e -> {
            if (client != null) {
                client.leaveTable();
            }
            showLobby();
        });

        JButton backBtn = new JButton("Back to Lobby");
        backBtn.addActionListener(e -> showLobby());

        controlRow.add(leaveBtn);
        controlRow.add(backBtn);

        rootPanel.add(Box.createVerticalStrut(20));
        rootPanel.add(controlRow);

        // ======================================================================
        // >>> PLAYER ACTION BUTTONS (Hit, Stand, Double, Bet) <<<
        // ======================================================================
        JPanel playerActions = new JPanel();
        playerActions.setOpaque(false);

        JButton hitBtn = new JButton("Hit");
        JButton standBtn = new JButton("Stand");
        JButton doubleBtn = new JButton("Double");
        JButton betBtn = new JButton("Bet");

        // Add to panel
        playerActions.add(hitBtn);
        playerActions.add(standBtn);
        playerActions.add(doubleBtn);
        playerActions.add(betBtn);

        // Add panel to GUI
        rootPanel.add(Box.createVerticalStrut(15));
        rootPanel.add(playerActions);

        // ---- BUTTON HANDLERS ----
        // ---- BUTTON HANDLERS ----
        // ---- BUTTON HANDLERS ----

        hitBtn.addActionListener(e -> {
            if (client != null) {
                TableSnapshot updated = client.sendPlayerAction(PlayerAction.HIT);
                if (updated != null) {
                    displayTable(updated);
                }
            }
        });

        standBtn.addActionListener(e -> {
            if (client != null) {
                TableSnapshot updated = client.sendPlayerAction(PlayerAction.STAND);
                if (updated != null) {
                    displayTable(updated);
                }
            }
        });

        doubleBtn.addActionListener(e -> {
            if (client != null) {
                TableSnapshot updated = client.sendPlayerAction(PlayerAction.DOUBLE);
                if (updated != null) {
                    displayTable(updated);
                }
            }
        });


        betBtn.addActionListener(e -> {
            String amtStr = JOptionPane.showInputDialog(frame, "Enter bet amount:");
            try {
                double amt = Double.parseDouble(amtStr);
                if (client != null) {
                    client.placeBet(amt);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid bet");
            }
        });

        // ======================================================================
        // >>> DEALER ACTION BUTTON (Start Round) — ONLY IF THIS USER IS DEALER <<<
        // ======================================================================
        if (client != null && client.isDealer()) {

            JPanel dealerActions = new JPanel();
            dealerActions.setOpaque(false);

            JButton startRoundBtn = new JButton("Start Round");
            dealerActions.add(startRoundBtn);

            startRoundBtn.addActionListener(e -> {
                if (client != null) {
                    client.startRound();
                }
            });

            rootPanel.add(Box.createVerticalStrut(15));
            rootPanel.add(dealerActions);
        }

        // ======================================================================
        // END BUTTON INSERTION
        // ======================================================================

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

    /** Starts a Swing timer that refreshes the table while we are on the TABLE screen. */
    private void startAutoRefresh() {
        // Avoid starting multiple timers
        if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
            return;
        }

        autoRefreshTimer = new javax.swing.Timer(1000, e -> {
            // Only refresh when we are actually viewing a table
            if (!"TABLE".equals(currentScreen)) {
                return;
            }

            if (client == null) {
                return;
            }

            TableSnapshot snap = client.requestTableState();
            if (snap != null) {
                displayTable(snap);
            }
        });

        autoRefreshTimer.start();
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
                // Invalid login
                JOptionPane.showMessageDialog(
                        frame,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                //clear the password field
                passField.setText("");
            }
        });
        
        registerBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Username and password cannot be empty.",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (client != null) {
                if (client.register(username, password, "PLAYER")) {
                    showLobby();
                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Registration failed.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
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
        
        // --- Populate table list from server ---
        if (tableModel == null) {
            tableModel = new DefaultListModel<>();
        }
        tableModel.clear();

        // Fetch real tables from server
        if (client != null && client.isLoggedIn()) {
            java.util.List<String> tables = client.requestTableList();  // <--- NEW

            if (tables != null) {
                for (String tableID : tables) {
                    tableModel.addElement("Table " + tableID);
                }
            } else {
                tableModel.addElement("No tables found");
            }

        } else {
            // Demo mode
            tableModel.addElement("Table T1");
        }

        if (tableList == null) {
            tableList = new JList<>(tableModel);
            tableList.setBackground(new Color(220, 220, 220));
        } else {
            tableList.setModel(tableModel);
        }


        
        JButton joinBtn = new JButton("Join Selected Table");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn = new JButton("Logout");
        JButton createTableBtn = new JButton("Create Table");

        // Add action listeners
       joinBtn.addActionListener(e -> {
            int index = tableList.getSelectedIndex();
            if (index >= 0) {
                String selected = tableModel.getElementAt(index);
                String tableId = selected.split(" ")[1];

                if (client != null) {
                    TableSnapshot snap = client.joinTable(tableId);
                    if (snap != null) displayTable(snap);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a table first");
            }
        });

        
        refreshBtn.addActionListener(e -> {
        if (client != null) {
            java.util.List<String> tables = client.requestTableList();

            tableModel.clear();
            if (tables != null) {
                for (String t : tables) {
                    tableModel.addElement("Table " + t);
                }
            }
        }
    });

        
        logoutBtn.addActionListener(e -> {
            if (client != null) {
                client.logout();
            }
            showLoginScreen();  // Go back to login
        });

        createTableBtn.addActionListener(e -> {
            if (client != null) {
                TableSnapshot snap = client.createTable();

                if (snap != null) {
                    JOptionPane.showMessageDialog(frame,
                        "Table created!\nTable ID: " + snap.getTableId(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    // Automatically join the newly created table
                    displayTable(snap);

                } else {
                    JOptionPane.showMessageDialog(frame,
                        "Failed to create table.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(joinBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(logoutBtn);
        buttonPanel.add(createTableBtn);

        
        rootPanel.add(title, BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(tableList), BorderLayout.CENTER);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        rootPanel.revalidate();
        rootPanel.repaint();
    }

        //Helper for win / lose hand visibility
    private String buildHandSummary(String who, List<CardView> cards) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }

        // Skip hidden cards when computing and formatting
        int total = computeHandValue(cards);
        StringBuilder handStr = new StringBuilder();
        boolean first = true;

        for (CardView card : cards) {
            if (card.isHidden()) {
                continue;
            }
            if (!first) {
                handStr.append(" ");
            } else {
                first = false;
            }
            handStr.append(rankToString(card.getRank()))
                .append(suitToSymbol(card.getSuit()));
        }

        if (handStr.length() == 0) {
            return null;
        }

        return who + " had " + total + " (" + handStr + ")";
    }

    // Standard blackjack total with aces as 1 or 11
    private int computeHandValue(List<CardView> cards) {
        int total = 0;
        int aces = 0;

        for (CardView card : cards) {
            if (card.isHidden()) {
                continue;
            }

            Rank r = card.getRank();
            switch (r) {
                case TWO:
                    total += 2;
                    break;
                case THREE:
                    total += 3;
                    break;
                case FOUR:
                    total += 4;
                    break;
                case FIVE:
                    total += 5;
                    break;
                case SIX:
                    total += 6;
                    break;
                case SEVEN:
                    total += 7;
                    break;
                case EIGHT:
                    total += 8;
                    break;
                case NINE:
                    total += 9;
                    break;
                case TEN:
                case JACK:
                case QUEEN:
                case KING:
                    total += 10;
                    break;
                case ACE:
                    total += 11;
                    aces++;
                    break;
            }
        }

        // Downgrade aces from 11 to 1 while over 21
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }

        return total;
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