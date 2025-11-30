package Server;

import Enums.MessageType;
import Message.Message;
import java.io.*;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central server entry point. Manages a shared LoginManager and
 * dispatches client connections to ClientHandler instances.
 */
public class Server {
    private static LoginManager manager;
    private static final int MAX_THREADS = 100;

    public static void main(String[] args) {
        manager = new LoginManager();
        System.out.println("Loading user data...");
        manager.loadData();
        System.out.println("User data loaded. Starting server.");

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
        ServerSocket server = null;
        int port = 8080;

        System.out.println("ServerSocket awaiting connections on port " + port + "...");

        try {
            server = new ServerSocket(port);
            server.setReuseAddress(true);

            while (true) {
                Socket client = server.accept();
                System.out.print("\nNew client connected: " + client.getInetAddress().getHostAddress() + "\n");
                ClientHandler clientSock = new ClientHandler(client, manager);
                pool.execute(clientSock);
            }
        } catch (IOException e) {
            System.err.println("Server encountered an I/O error: " + e.getMessage());
        } finally {
            if (server != null) {
                try {
                    server.close();
                    System.out.println("Server socket closed. Shutting down LoginManager.");
                    manager.save();
                    pool.shutdown();
                } catch (IOException e) {
                    System.err.println("Error closing server: " + e.getMessage());
                }
            }
        }
    }

    /**
     * ClientHandler implementation embedded to avoid separate file.
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final String clientID;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean connected;
        private final LoginManager manager;
        private Account account;

        public ClientHandler(Socket socket, LoginManager manager) {
            this.socket = socket;
            this.manager = manager;
            this.clientID = UUID.randomUUID().toString();
            this.connected = false;
            this.account = null;
        }

        @Override
        public void run() {
            try {
                this.out = new ObjectOutputStream(socket.getOutputStream());
                this.out.flush();
                this.in = new ObjectInputStream(socket.getInputStream());

                System.out.println("[Server] New client connected: " + clientID);
                connected = true;

                while (connected) {
                    try {
                        Message msg = (Message) in.readObject();
                        handleMessage(msg);
                    } catch (EOFException e) {
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

        private void handleLogin(Message msg) {
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

            Account authenticatedAccount = manager.login(username, password);

            if (authenticatedAccount != null) {
                this.account = authenticatedAccount;
                sendMessage(createOKResponse(msg, authenticatedAccount));
                System.out.println("[Server] User " + username + " logged in.");
                UserLogger.log(username, "LOGIN");
            } else {
                sendMessage(createErrorResponse(msg, "Invalid credentials or account not active"));
            }
        }

        private void handleLogout(Message msg) {
            if (account != null) {
                System.out.println("[Server] User " + account.getUsername() + " logged out.");
                UserLogger.log(account.getUsername(), "LOGOUT");
                account = null;
                sendMessage(createOKResponse(msg, "Logout successful"));
                connected = false;
            } else {
                sendMessage(createErrorResponse(msg, "No active session"));
            }
        }

        private void handleRegister(Message msg) {
            if (!(msg.getPayload() instanceof String[])) {
                sendMessage(createErrorResponse(msg, "Invalid registration payload"));
                return;
            }

            String[] regData = (String[]) msg.getPayload();
            if (regData.length < 3) {
                sendMessage(createErrorResponse(msg, "Missing registration data"));
                return;
            }

            String username = regData[0];
            String password = regData[1];
            String type = regData[2];

            try {
                manager.createAccount(username, password, type);
                sendMessage(createOKResponse(msg, "Account created successfully"));
                System.out.println("[Server] New account registered: " + username);
                UserLogger.log(username, "REGISTER");
            } catch (IllegalArgumentException e) {
                sendMessage(createErrorResponse(msg, e.getMessage()));
            }
        }

        private void handleBetPlaced(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }
            sendMessage(createOKResponse(msg, "Bet received"));
        }

        private void handlePlayerAction(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }
            sendMessage(createOKResponse(msg, "Action received"));
        }

        private void handleJoinTable(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }
            sendMessage(createOKResponse(msg, "Joined table"));
        }

        private void handleLeaveTable(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }
            sendMessage(createOKResponse(msg, "Left table"));
        }

        private void handleRequestProfile(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }
            sendMessage(createOKResponse(msg, "Profile retrieved"));
        }

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

        public Account getAccount() {
            return account;
        }

        public String getClientID() {
            return clientID;
        }

        public boolean isConnected() {
            return connected;
        }

        private void cleanup() {
            try {
                connected = false;
                if (account != null) {
                    manager.logout(account);
                }
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null && !socket.isClosed()) socket.close();
                System.out.println("[Server] ClientHandler " + clientID + " cleaned up.");
            } catch (IOException e) {
                System.err.println("[Server] Error during cleanup: " + e.getMessage());
            }
        }

        private Message createOKResponse(Message request, Object payload) {
            return new Message(
                UUID.randomUUID().toString(),
                MessageType.OK,
                "SERVER",
                request.getSender(),
                payload,
                java.time.LocalDateTime.now()
            );
        }

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
}


