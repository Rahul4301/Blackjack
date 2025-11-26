package Server;

import Message.Message;
import Enums.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Blackjack server that manages client connections and game state.
 * Listens on a port, accepts client connections, spawns ClientHandlers.
 */
public class Server {
    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    private List<ClientHandler> connectedClients;
    private LoginManager loginManager;
    private Map<String, GameTable> gameTables;

    public Server(int port) {
        this.port = port;
        this.running = false;
        this.connectedClients = new CopyOnWriteArrayList<>();
        this.loginManager = new LoginManager();
        this.gameTables = new HashMap<>();
    }

    /**
     * Start the server and listen for incoming client connections.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("[Server] Started on port " + port);

            // Accept client connections in a loop
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Incoming connection from " + clientSocket.getInetAddress());

                // Create and start a new ClientHandler thread
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

        // Close all client handlers
        for (ClientHandler handler : connectedClients) {
            // ClientHandler will cleanup on next message read or timeout
        }
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
                // Match by client ID or account username
                if (handler.getClientID().equals(identifier) ||
                    (handler.getAccount() != null && handler.getAccount().getUsername().equals(identifier))) {
                    handler.sendMessage(msg);
                    return;
                }
            }
        }
        System.out.println("[Server] Client not found: " + identifier);
    }

    /**
     * Handle an incoming message (can route to GameTable, etc).
     */
    public void handleMessage(Message msg) {
        System.out.println("[Server] Processing message: " + msg.getMessageType());
        // TODO: Route to appropriate handler (GameTable, etc)
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
     * Get or create a game table by ID.
     */
    public GameTable getGameTable(String tableID) {
        return gameTables.get(tableID);
    }

    /**
     * Register a new game table.
     */
    public void registerGameTable(GameTable table) {
        gameTables.put(table.getTableID(), table);
    }

    /**
     * Entry point to start the server.
     */
    public static void main(String[] args) {
        int port = 5000; // default port
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
