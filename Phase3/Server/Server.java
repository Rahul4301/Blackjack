package Server;

import Enums.MessageType;
import Message.Message;
import Shared.TableSnapshot;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central server entry point. Manages a shared LoginManager and
 * dispatches client connections to ClientHandler instances.
 */
public class Server {
    private static LoginManager manager;
    private static final int MAX_THREADS = 100;

    // Tables by ID
    private static final Map<String, GameTable> tables = new ConcurrentHashMap<>();

    // For each table ID, which client handlers are attached (dealer + players)
    private static final Map<String, List<ClientHandler>> tableClients = new ConcurrentHashMap<>();

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
        private GameTable table;

        // new - which table this connection is attached to, if any
        private GameTable currentTable;
        private String currentTableId;

        public ClientHandler(Socket socket, LoginManager manager) {
            this.socket = socket;
            this.manager = manager;
            this.clientID = UUID.randomUUID().toString();
            this.connected = false;
            this.account = null;
            this.currentTable = null;
            this.currentTableId = null;
            this.table = null;
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

       /*




       MAIN MESSAGE HANDLER
       
       
       
       
       */
        
        
        
        

        private void handleMessage(Message msg) {
            System.out.println("[Server] Received from " + clientID + ": " + msg.getMessageType());

            switch (msg.getMessageType()) {
                case LOGIN:
                    handleLogin(msg);//done
                    break;
                case LOGOUT:
                    handleLogout(msg);//done
                    break;
                case REGISTER:
                    handleRegister(msg);//done
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
                case CREATE_TABLE:         
                    handleCreateTable(msg);//done (?)
                    break;
                case LIST_TABLES:          
                    handleListTables(msg);
                    break;
                case REQUEST_TABLE_STATE:
                    handleRequestTableState(msg);
                    break;
                case EXIT:
                    handleExit(msg); //done
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
            } else {
                sendMessage(createErrorResponse(msg, "No active session"));
            }
        }

        private Account handleRegister(Message msg) {
            if (!(msg.getPayload() instanceof String[])) {
                sendMessage(createErrorResponse(msg, "Invalid registration payload"));
                return null;
            }

            String[] regData = (String[]) msg.getPayload();
            if (regData.length < 3) {
                sendMessage(createErrorResponse(msg, "Missing registration data"));
                return null;
            }

            String username = regData[0];
            String password = regData[1];
            String type = regData[2];

            try {
                Account newAccount = manager.createAccount(username, password, type);
                sendMessage(createOKResponse(msg, newAccount));
                System.out.println("[Server] New account registered: " + username);
                UserLogger.log(username, "REGISTER");
                this.account = newAccount;
                return newAccount;
            } catch (IllegalArgumentException e) {
                sendMessage(createErrorResponse(msg, e.getMessage()));
            }
            return null;
        }

        private void handleBetPlaced(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }
            sendMessage(createOKResponse(msg, "received"));
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

                // after adding the player to the table
                // Snapshot for this player (now keyed by username)
                TableSnapshot snapshotForPlayer = table.createSnapshotFor(account.getUsername());
                sendMessage(createOKResponse(msg, snapshotForPlayer));


                return;
            }

            if (!(account instanceof Player player)) {
                sendMessage(createErrorResponse(msg, "Only players can join tables"));
                return;
            }

            if (!(msg.getPayload() instanceof String)) {
                sendMessage(createErrorResponse(msg, "Expected table ID as payload"));
                return;
            }

            String tableId = (String) msg.getPayload();
            GameTable table = tables.get(tableId);
            if (table == null) {
                sendMessage(createErrorResponse(msg, "Unknown table: " + tableId));
                return;
            }

            boolean added = table.addPlayer(player);
            if (!added) {
                sendMessage(createErrorResponse(msg, "Table is full"));
                return;
            }

            // Track attachment
            currentTable = table;
            currentTableId = tableId;
            tableClients.computeIfAbsent(tableId, k -> new ArrayList<>()).add(this);

            System.out.println("[Server] Player " + player.getUsername() + " joined table " + tableId);

            // Snapshot for this player
            TableSnapshot snapshotForPlayer = table.createSnapshotFor(player.getUsername());
            sendMessage(createOKResponse(msg, snapshotForPlayer));

            // Notify everyone else at that table
            broadcastSnapshot(table);
        }

        private void handleLeaveTable(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }

            if (currentTable == null || currentTableId == null) {
                sendMessage(createErrorResponse(msg, "Not currently at a table"));
                return;
            }

            GameTable table = currentTable;
            String tableId = currentTableId;

            // Remove this handler from subscribers
            List<ClientHandler> list = tableClients.get(tableId);
            if (list != null) {
                list.remove(this);
                if (list.isEmpty()) {
                    tableClients.remove(tableId);
                }
            }

            if (account instanceof Player player) {
                table.removePlayer(player);
                System.out.println("[Server] Player " + player.getUsername() + " left table " + tableId);
            } else if (account instanceof Dealer dealer) {
                // Dealer leaves - for now, just log it
                System.out.println("[Server] Dealer " + dealer.getUsername() + " detached from table " + tableId);
            }

            currentTable = null;
            currentTableId = null;

            sendMessage(createOKResponse(msg, "Left table"));

            // Notify remaining clients at that table
            if (tables.containsKey(tableId)) {
                broadcastSnapshot(table);
            }
        }

        private void handleRequestProfile(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }
            sendMessage(createOKResponse(msg, "Profile retrieved"));
        }

        private void handleCreateTable(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }

            if (!(account instanceof Dealer dealer)) {
                sendMessage(createErrorResponse(msg, "Only dealers can create tables"));
                return;
            }

            // Create a new table for this dealer
            GameTable table = new GameTable(dealer);
            String tableId = table.getTableID();
            tables.put(tableId, table);

            // Attach this connection to the table
            currentTable = table;
            currentTableId = tableId;
            tableClients.computeIfAbsent(tableId, k -> new ArrayList<>()).add(this);

            // Initial snapshot for dealer (no "you" flag, so pass null as player id)
            TableSnapshot snapshot = table.createSnapshotFor(null);

            System.out.println("[Server] Dealer " + dealer.getUsername() + " created table " + tableId);
            sendMessage(createOKResponse(msg, snapshot));
        }

        private void handleRequestTableState(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }
            if (currentTable == null) {
                sendMessage(createErrorResponse(msg, "Not currently at a table"));
                return;
            }

            GameTable table = currentTable;

            TableSnapshot snapshot;
            if (account instanceof Player p) {
                snapshot = table.createSnapshotFor(p.getUsername());
            } else {
                // dealer or something else
                snapshot = table.createSnapshotFor(null);
            }

            sendMessage(createOKResponse(msg, snapshot));
        }



        private void handleExit(Message msg){
            
            sendMessage(createOKResponse(msg, "See you next time!"));
            connected = false;
        }

        private void broadcastSnapshot(GameTable table) {
            String tableId = table.getTableID();
            List<ClientHandler> list = tableClients.get(tableId);
            if (list == null || list.isEmpty()) {
                return;
            }

            for (ClientHandler handler : list) {
                TableSnapshot snapshot;

                if (handler.account instanceof Player p) {
                    // pass username here
                    snapshot = table.createSnapshotFor(p.getUsername());
                } else {
                    // Dealer or unknown, no "you" flag
                    snapshot = table.createSnapshotFor(null);
                }

                Message snapshotMsg = new Message(
                        UUID.randomUUID().toString(),
                        MessageType.TABLE_SNAPSHOT,
                        "SERVER",
                        handler.clientID,
                        snapshot,
                        java.time.LocalDateTime.now()
                );
                handler.sendMessage(snapshotMsg);
            }
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

        

        private void handleListTables(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }

            List<String> ids = new ArrayList<>();
            for (GameTable t : tables.values()) {
                ids.add(t.getTableID());
            }

            sendMessage(createOKResponse(msg, ids));
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
    
     /* =========================
       Main entry point
       ========================= */    
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

}


