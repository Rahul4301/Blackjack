package Server;

import Message.Message;
import Enums.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Blackjack server that manages client connections and game tables.
 * Listens on a port, accepts client connections, spawns ClientHandlers.
 */
public class Server {
    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    private List<ClientHandler> connectedClients;
    private LoginManager loginManager;
    private Map<String, GameTable> gameTables;
    private Dealer dealer;
    private static final double MIN_BET = 10.0;
    private static final double MAX_BET = 1000.0;

    public Server(int port) {
        this.port = port;
        this.running = false;
        this.connectedClients = new CopyOnWriteArrayList<>();
        this.loginManager = new LoginManager();
        this.gameTables = new HashMap<>();
        this.dealer = new Dealer("DEALER", "");
    }

    /**
     * Start the server and listen for incoming client connections.
     */
    public void start() {
        // Load accounts from database
        loginManager.loadData();
        
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("[Server] Started on port " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Incoming connection from " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                connectedClients.add(handler);
                new Thread(handler, "ClientHandler-" + handler.getClientID()).start();
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[Server] Error accepting client: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    /**
     * Stop the server and close all connections.
     */
    public void stop() {
        running = false;
        System.out.println("[Server] Shutting down...");
        
        // Save all accounts before closing
        loginManager.save();
        
        connectedClients.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[Server] Error closing server socket: " + e.getMessage());
        }

        System.out.println("[Server] Stopped.");
    }

    /**
     * Broadcast a message to all connected clients.
     */
    public void broadcastUpdate(Message msg) {
        for (ClientHandler handler : connectedClients) {
            if (handler.isConnected()) {
                handler.sendMessage(msg);
            }
        }
    }

    /**
     * Send a message to a specific client by username or ID.
     */
    public void sendToClient(String identifier, Message msg) {
        for (ClientHandler handler : connectedClients) {
            if (handler.isConnected()) {
                if (handler.getClientID().equals(identifier) ||
                    (handler.getAccount() != null && handler.getAccount().getUsername().equals(identifier))) {
                    handler.sendMessage(msg);
                    return;
                }
            }
        }
    }

    /**
     * Broadcast a game update to all players at a specific table.
     */
    public void broadcastToTable(GameTable table, Message msg) {
        if (table == null) return;
        for (Player player : table.getPlayers()) {
            sendToClient(player.getUsername(), msg);
        }
    }

    /**
     * Create a new game table.
     */
    public GameTable createGameTable() {
        GameTable table = new GameTable(dealer, MIN_BET, MAX_BET);
        table.setServer(this);  // Give table access to server for broadcasting
        gameTables.put(table.getTableID(), table);
        System.out.println("[Server] Created table: " + table.getTableID());
        return table;
    }

    /**
     * Get a game table by ID.
     */
    public GameTable getGameTable(String tableID) {
        return gameTables.get(tableID);
    }

    /**
     * Get all active game tables.
     */
    public Collection<GameTable> getAllGameTables() {
        return gameTables.values();
    }

    /**
     * Remove a game table (when empty or closed).
     */
    public void removeGameTable(String tableID) {
        gameTables.remove(tableID);
    }

    /**
     * Remove a disconnected client handler from the registry.
     */
    public void removeClientHandler(ClientHandler handler) {
        connectedClients.remove(handler);
        System.out.println("[Server] Removed client handler. Active clients: " + connectedClients.size());
    }

    /**
     * Get the login manager.
     */
    public LoginManager getLoginManager() {
        return loginManager;
    }

    /**
     * Get all connected clients (read-only).
     */
    public List<ClientHandler> getConnectedClients() {
        return new ArrayList<>(connectedClients);
    }

    /**
     * Get number of active connections.
     */
    public int getActiveConnectionCount() {
        return connectedClients.size();
    }

    /**
     * Entry point to start the server.
     */
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port. Using default: " + port);
            }
        }

        Server server = new Server(port);
        server.start();
    }
}
