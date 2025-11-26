package Server;
import Enums.MessageType;
import Message.Message;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

/**
 * Handles a single client connection in a separate thread.
 * Reads messages from client, processes them, and sends responses.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private String clientID;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected;
    private Account account; // authenticated account, null until login

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.clientID = UUID.randomUUID().toString();
        this.connected = true;
        this.account = null;
    }

    @Override
    public void run() {
        try {
            // Initialize streams (output first to avoid deadlock)
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());

            System.out.println("[Server] New client connected: " + clientID);

            // Listen for incoming messages
            while (connected) {
                try {
                    Message msg = (Message) in.readObject();
                    handleMessage(msg);
                } catch (EOFException e) {
                    // Client disconnected gracefully
                    System.out.println("[Server] Client " + clientID + " disconnected.");
                    connected = false;
                } catch (ClassNotFoundException e) {
                    System.err.println("[Server] Unknown message type: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Error in ClientHandler: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Process incoming message and dispatch to appropriate handler.
     */
    private void handleMessage(Message msg) {
        System.out.println("[Server] Received from " + clientID + ": " + msg.getMessageType());

        switch (msg.getMessageType()) {
            case LOGIN:
                handleLogin(msg);
                break;
            case LOGOUT:
                handleLogout(msg);
                break;
            case REGISTER:
                handleRegister(msg);
                break;
            case BET_PLACED:
                handleBetPlaced(msg);
                break;
            case PLAYER_ACTION:
                handlePlayerAction(msg);
                break;
            case JOIN_TABLE:
                handleJoinTable(msg);
                break;
            case LEAVE_TABLE:
                handleLeaveTable(msg);
                break;
            case REQUEST_PROFILE:
                handleRequestProfile(msg);
                break;
            default:
                System.out.println("[Server] Unhandled message type: " + msg.getMessageType());
        }
    }

    /**
     * Handle login request.
     */
    private void handleLogin(Message msg) {
        // Payload should contain [username, password]
        if (!(msg.getPayload() instanceof String[])) {
            sendMessage(createErrorResponse(msg, "Invalid login payload"));
            return;
        }

        String[] creds = (String[]) msg.getPayload();
        if (creds.length < 2) {
            sendMessage(createErrorResponse(msg, "Missing credentials"));
            return;
        }

        String username = creds[0];
        String password = creds[1];

        // Authenticate via LoginManager
        LoginManager loginMgr = server.getLoginManager();
        Account authenticatedAccount = loginMgr.authenticate(username, password);

        if (authenticatedAccount != null) {
            this.account = authenticatedAccount;
            sendMessage(createOKResponse(msg, "Login successful"));
            System.out.println("[Server] User " + username + " logged in.");
        } else {
            sendMessage(createErrorResponse(msg, "Invalid credentials or account not active"));
        }
    }

    /**
     * Handle logout request.
     */
    private void handleLogout(Message msg) {
        if (account != null) {
            server.getLoginManager().logout(account);
            System.out.println("[Server] User " + account.getUsername() + " logged out.");
            account = null;
            sendMessage(createOKResponse(msg, "Logout successful"));
        } else {
            sendMessage(createErrorResponse(msg, "No active session"));
        }
    }

    /**
     * Handle registration (placeholder).
     */
    private void handleRegister(Message msg) {
        // TODO: Implement user registration logic
        sendMessage(createErrorResponse(msg, "Registration not yet implemented"));
    }

    /**
     * Handle bet placement (placeholder).
     */
    private void handleBetPlaced(Message msg) {
        if (account == null) {
            sendMessage(createErrorResponse(msg, "Not logged in"));
            return;
        }
        // TODO: Validate bet and integrate with GameTable
        sendMessage(createOKResponse(msg, "Bet received"));
    }

    /**
     * Handle player action (hit/stand/double/split).
     */
    private void handlePlayerAction(Message msg) {
        if (account == null) {
            sendMessage(createErrorResponse(msg, "Not logged in"));
            return;
        }
        // TODO: Forward to GameTable
        sendMessage(createOKResponse(msg, "Action received"));
    }

    /**
     * Handle join table request.
     */
    private void handleJoinTable(Message msg) {
        if (account == null) {
            sendMessage(createErrorResponse(msg, "Not logged in"));
            return;
        }
        // TODO: Add player to GameTable
        sendMessage(createOKResponse(msg, "Joined table"));
    }

    /**
     * Handle leave table request.
     */
    private void handleLeaveTable(Message msg) {
        if (account == null) {
            sendMessage(createErrorResponse(msg, "Not logged in"));
            return;
        }
        // TODO: Remove player from GameTable
        sendMessage(createOKResponse(msg, "Left table"));
    }

    /**
     * Handle profile request.
     */
    private void handleRequestProfile(Message msg) {
        if (account == null) {
            sendMessage(createErrorResponse(msg, "Not logged in"));
            return;
        }
        // TODO: Return player profile (if Player) or dealer info (if Dealer)
        sendMessage(createOKResponse(msg, "Profile retrieved"));
    }

    /**
     * Send a message to this client.
     */
    public void sendMessage(Message msg) {
        if (!connected || out == null) {
            System.err.println("[Server] Cannot send to " + clientID + ": not connected");
            return;
        }

        try {
            synchronized (out) {
                out.writeObject(msg);
                out.flush();
            }
            System.out.println("[Server] Sent to " + clientID + ": " + msg.getMessageType());
        } catch (IOException e) {
            System.err.println("[Server] Failed to send to " + clientID + ": " + e.getMessage());
            connected = false;
        }
    }

    /**
     * Get the authenticated account (null if not logged in).
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Get the client ID.
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * Check if handler is connected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Cleanup and close resources.
     */
    private void cleanup() {
        try {
            connected = false;
            if (account != null) {
                server.getLoginManager().logout(account);
            }
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            server.removeClientHandler(this);
            System.out.println("[Server] ClientHandler " + clientID + " cleaned up.");
        } catch (IOException e) {
            System.err.println("[Server] Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Helper: create an OK response message.
     */
    private Message createOKResponse(Message request, String payload) {
        return new Message(
            UUID.randomUUID().toString(),
            MessageType.OK,
            "SERVER",
            request.getSender(),
            payload,
            java.time.LocalDateTime.now()
        );
    }

    /**
     * Helper: create an error response message.
     */
    private Message createErrorResponse(Message request, String errorMsg) {
        return new Message(
            UUID.randomUUID().toString(),
            MessageType.ERROR,
            "SERVER",
            request.getSender(),
            errorMsg,
            java.time.LocalDateTime.now()
        );
    }
}
