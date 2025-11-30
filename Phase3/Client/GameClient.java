package Client;

import Enums.MessageType;
import Message.Message;
import Server.GameTable;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Enhanced Blackjack Client with proper networking and messaging.
 * Handles connection, authentication, and game communication.
 */
public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected;
    private String clientID;
    private String currentUsername;
    private double currentBalance;
    private GameTable currentTable;
    private Menu menu;
    private BlockingQueue<Message> messageQueue;
    private Thread messageReceiver;

    public GameClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        this.connected = true;
        this.clientID = "CLIENT_" + System.currentTimeMillis();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.menu = new Menu(this);
        
        // Start message receiver thread
        startMessageReceiver();
        
        System.out.println("[Client] Connected to " + host + ":" + port);
    }

    /**
     * Start background thread to receive messages from server.
     */
    private void startMessageReceiver() {
        messageReceiver = new Thread(() -> {
            while (connected) {
                try {
                    Message msg = (Message) in.readObject();
                    messageQueue.offer(msg);
                    handleMessage(msg);
                } catch (EOFException e) {
                    System.out.println("[Client] Server closed connection.");
                    connected = false;
                } catch (ClassNotFoundException | IOException e) {
                    if (connected) {
                        System.err.println("[Client] Error receiving message: " + e.getMessage());
                    }
                }
            }
        });
        messageReceiver.setDaemon(true);
        messageReceiver.start();
    }

    /**
     * Send a message to the server.
     */
    public void sendMessage(Message msg) {
        if (!connected) {
            System.err.println("[Client] Not connected to server.");
            return;
        }
        try {
            synchronized (out) {
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("[Client] Error sending message: " + e.getMessage());
            connected = false;
        }
    }

    /**
     * Handle incoming messages from server.
     */
    private void handleMessage(Message msg) {
        switch (msg.getMessageType()) {
            case OK:
                System.out.println("[Server] " + msg.getPayload());
                break;
            case ERROR:
                System.err.println("[Server] Error: " + msg.getPayload());
                break;
            case GAME_UPDATE:
                if (msg.getPayload() instanceof GameTable) {
                    currentTable = (GameTable) msg.getPayload();
                    menu.displayGameTable(currentTable);
                }
                break;
            default:
                System.out.println("[Server] " + msg.getMessageType() + ": " + msg.getPayload());
        }
    }

    /**
     * Login user with username and password.
     */
    public boolean login(String username, String password) {
        Message loginMsg = new Message(
            MessageType.LOGIN,
            clientID,
            "SERVER",
            username + "," + password,
            LocalDateTime.now()
        );
        sendMessage(loginMsg);
        currentUsername = username;
        return true;
    }

    /**
     * Register new player account.
     */
    public void register(String username, String password) {
        Message registerMsg = new Message(
            MessageType.REGISTER,
            clientID,
            "SERVER",
            username + "," + password + ",PLAYER",
            LocalDateTime.now()
        );
        sendMessage(registerMsg);
    }

    /**
     * Request to join a game table.
     */
    public void joinTable() {
        Message joinMsg = new Message(
            MessageType.JOIN_TABLE,
            clientID,
            "SERVER",
            "JOIN",
            LocalDateTime.now()
        );
        sendMessage(joinMsg);
    }

    /**
     * Request to leave current table.
     */
    public void leaveTable() {
        Message leaveMsg = new Message(
            MessageType.LEAVE_TABLE,
            clientID,
            "SERVER",
            "LEAVE",
            LocalDateTime.now()
        );
        sendMessage(leaveMsg);
        currentTable = null;
    }

    /**
     * Place a bet for the current round.
     */
    public void placeBet(int amount) {
        Message betMsg = new Message(
            MessageType.BET_PLACED,
            clientID,
            "SERVER",
            String.valueOf(amount),
            LocalDateTime.now()
        );
        sendMessage(betMsg);
    }

    /**
     * Perform a player action (hit/stand/double/split).
     */
    public void performAction(String action) {
        Message actionMsg = new Message(
            MessageType.PLAYER_ACTION,
            clientID,
            "SERVER",
            action.toLowerCase(),
            LocalDateTime.now()
        );
        sendMessage(actionMsg);
    }

    /**
     * Request player profile.
     */
    public void requestProfile() {
        Message profileMsg = new Message(
            MessageType.REQUEST_PROFILE,
            clientID,
            "SERVER",
            "PROFILE",
            LocalDateTime.now()
        );
        sendMessage(profileMsg);
    }

    /**
     * Logout and disconnect.
     */
    public void logout() {
        Message logoutMsg = new Message(
            MessageType.LOGOUT,
            clientID,
            "SERVER",
            currentUsername,
            LocalDateTime.now()
        );
        sendMessage(logoutMsg);
        disconnect();
    }

    /**
     * Disconnect from server.
     */
    public void disconnect() {
        try {
            connected = false;
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("[Client] Disconnected.");
        } catch (IOException e) {
            System.err.println("[Client] Error disconnecting: " + e.getMessage());
        }
    }

    /**
     * Get current table (for menu display).
     */
    public GameTable getCurrentTable() {
        return currentTable;
    }

    /**
     * Get current username.
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Check if connected to server.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Start the client interactive menu.
     */
    public void startMenu() {
        menu.displayMainMenu();
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter server host [localhost]: ");
        String host = sc.nextLine().trim();
        if (host.isEmpty()) host = "localhost";

        System.out.print("Enter server port [8080]: ");
        String portStr = sc.nextLine().trim();
        int port = 8080;
        try {
            if (!portStr.isEmpty()) port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port, using 8080.");
        }

        try {
            GameClient client = new GameClient(host, port);
            client.startMenu();
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}
