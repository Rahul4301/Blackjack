package Client;

import java.awt.*;
import javax.swing.*;

public class GUI {

    private JFrame frame;
    private JPanel mainPanel;
    private String currentScreen;

    public GUI() {
        this.currentScreen = "LOGIN";
        initFrame();
        // For now, start on a green "Login" screen
        showGreenScreen("Login Screen");
    }

    /** Set up the main window */
    private void initFrame() {
        frame = new JFrame("Blackjack Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null); // center on screen

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Default background: casino green
        mainPanel.setBackground(new Color(0, 120, 0));

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    /** Core helper: show a full green screen with centered label text */
    private void showGreenScreen(String titleText) {
        mainPanel.removeAll();

        mainPanel.setBackground(new Color(0, 120, 0)); // nice table green

        JLabel label = new JLabel(titleText, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 36f));

        mainPanel.add(label, BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ====== Public methods used by MainApp ======

    public void displayLoginScreen() {
        currentScreen = "LOGIN";
        showGreenScreen("Login Screen");
    }

    public void displayLobby() {
        currentScreen = "LOBBY";
        showGreenScreen("Game Lobby");
    }

    public void displayTable() {
        currentScreen = "TABLE";
        showGreenScreen("Blackjack Table");
    }

    public void updateGameState() {
        // later: update table UI, cards, bets...
        System.out.println("Updating game state (GUI stub)");
    }

    public void showPlayerOptions() {
        // later: show buttons for Hit / Stand / Double / Split
        System.out.println("Showing player options (GUI stub)");
    }

    public void showDealerOptions() {
        // later: show dealer controls
        System.out.println("Showing dealer options (GUI stub)");
    }

    public void showAnimations() {
        // later: animate dealing, chips, etc.
        System.out.println("Showing animations (GUI stub)");
    }

    public void displayError(String message) {
        JOptionPane.showMessageDialog(
            frame,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public void displayResult() {
        // later: show win/lose/push result on the green screen
        System.out.println("Displaying game result (GUI stub)");
    }

    public boolean askPlayAgain() {
        int result = JOptionPane.showConfirmDialog(
            frame,
            "Play another hand?",
            "Play Again",
            JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }

    public void shutdown() {
        if (frame != null) {
            frame.dispose();
        }
        System.out.println("Shutting down GUI and closing connections");
    }
}
