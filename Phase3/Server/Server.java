package Server;

import Enums.GameState;
import Enums.MessageType;
import Enums.PlayerAction;
import Message.Message;
import Shared.TableSnapshot;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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
                case DEPOSIT:
                    handleDeposit(msg); //done
                    break;
                case BET_PLACED:
                    handleBetPlaced(msg); //done
                    break;
                case PLAYER_ACTION:
                    handlePlayerAction(msg);
                    break;
                case JOIN_TABLE:
                    handleJoinTable(msg); //done
                    break;
                case LEAVE_TABLE:
                    handleLeaveTable(msg); //done
                    break;
                case REQUEST_PROFILE:
                    handleRequestProfile(msg);
                    break;
                case CREATE_TABLE:         
                    handleCreateTable(msg);//done 
                    break;
                case LIST_TABLES:          
                    handleListTables(msg);
                    break;
                case REQUEST_TABLE_STATE:
                    handleRequestTableState(msg); //done (?)
                    break;
                case START:
                    handleStart(msg);
                    break;
                case EXIT:
                    handleExit(msg); //done
                    break;
                default:
                    System.out.println("[Server] Unhandled message type: " + msg.getMessageType());
            }
        }

        private void handleStart(Message msg) {
            if (currentTable == null) {
                sendMessage(createErrorResponse(msg, "Not currently attached to a table"));
                return;
            }

            GameTable table = currentTable;

            // If we are coming from a finished round, reset everything
            if (table.getState() == GameState.RESULTS) {
                table.resetForNextRound();
            }

            boolean started = table.startRound();
            if (!started) {
                sendMessage(createErrorResponse(msg,
                        "Error starting round. Make sure the table is in BETTING state and at least one player has a valid bet."));
                return;
            }

            TableSnapshot snapshot = table.createSnapshotFor(null);
            sendMessage(createOKResponse(msg, snapshot));
        }


        private void handleDeposit(Message msg){
            // 1. Validate payload type
            if(!(msg.getPayload() instanceof String)){
                sendMessage(createErrorResponse(msg, "Invalid deposit payload"));
                return;
            }

            String depositStr = (String) msg.getPayload();
            // 2. Validate that the string is a number
            double deposit;
            try {
                deposit = Double.parseDouble(depositStr);
            } catch(NumberFormatException e) {
                sendMessage(createErrorResponse(msg, "Deposit amount must be a valid number"));
                return;
            }

            // 3. Validate deposit is positive
            if(deposit <= 0){
                sendMessage(createErrorResponse(msg, "Deposit amount must be greater than zero"));
                return;
            }

            // 4. Apply deposit to the account
            // Replace "account" with however you track accounts
            // Example:
            // account.deposit(deposit);
            if(account instanceof Player){
                ((Player)account).updateBalance(deposit);
                sendMessage(createOKResponse(msg, "Deposit successful: " + deposit));
                return;
            }

            sendMessage(createErrorResponse(msg, "Deposit failed, account not found"));
            return;
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

            if (!(account instanceof Player player)) {
                sendMessage(createErrorResponse(msg, "Only players can place bets"));
                return;
            }

            if (currentTable == null) {
                sendMessage(createErrorResponse(msg, "Not currently at a table"));
                return;
            }

            Object payload = msg.getPayload();
            if (!(payload instanceof Double amount)) {
                sendMessage(createErrorResponse(msg, "Expected bet amount (Double) as payload"));
                return;
            }

            // Place the bet on this player's object
            player.placeBet(amount);

            GameTable table = currentTable;

            // Do NOT start the round here any more.
            // Just broadcast an updated snapshot so everyone sees the bet.
            broadcastSnapshot(table);
        }



        private void handlePlayerAction(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }

            if (!(account instanceof Player player)) {
                sendMessage(createErrorResponse(msg, "Only players can act on a hand"));
                return;
            }

            if (currentTable == null) {
                sendMessage(createErrorResponse(msg, "Not currently at a table"));
                return;
            }

            Object payload = msg.getPayload();
            if (!(payload instanceof PlayerAction playerAction)) {
                sendMessage(createErrorResponse(msg, "Expected action PlayerAction payload (e.g., HIT or STAND)"));
                return;
            }

            GameTable table = currentTable;

            // Use the existing blackjack engine in GameTable
            boolean applied = table.handlePlayerAction(player.getUsername(), playerAction);
            if (!applied) {
                // Wrong state, not player's turn, etc
                sendMessage(createErrorResponse(msg, "Action not allowed at this time"));
                return;
            }

            TableSnapshot snap = table.createSnapshotFor(player.getUsername());
            sendMessage(createOKResponse(msg, snap));

        }


       private void handleJoinTable(Message msg) {
            if (account == null) {
                sendMessage(createErrorResponse(msg, "Not logged in"));
                return;
            }

            if (!(account instanceof Player player)) {
                sendMessage(createErrorResponse(msg, "Only players can join tables"));
                return;
            }

            player.resetForNewRound();

            if (!(msg.getPayload() instanceof String)) {
                sendMessage(createErrorResponse(msg, "Expected table ID as payload"));
                return;
            }

            String tableId = (String) msg.getPayload();
            GameTable table = tables.get(tableId);
            if (table == null) {
                sendMessage(createErrorResponse(msg, "Table not found: " + tableId));
                return;
            }

            boolean added = table.addPlayer(player);
            if (!added) {
                sendMessage(createErrorResponse(msg, "Table is full"));
                return;
            }

            // Track which table this client is at
            currentTable = table;
            currentTableId = tableId;
            tableClients.computeIfAbsent(tableId, k -> new ArrayList<>()).add(this);

            System.out.println("[Server] Player " + player.getUsername() + " joined table " + tableId);

            // Start the 30 second countdown if this is the first player at this table
            scheduleAutoStartIfNeeded(table);

            // Send an initial snapshot to this player
            TableSnapshot snapshotForPlayer = table.createSnapshotFor(player.getUsername());
            sendMessage(createOKResponse(msg, snapshotForPlayer));

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
                player.resetForNewRound();
                System.out.println("[Server] Player " + player.getUsername() + " left table " + tableId);
            } else if (account instanceof Dealer dealer) {
                // Dealer leaves - for now, just log it
                System.out.println("[Server] Dealer " + dealer.getUsername() + " detached from table " + tableId);
            }
            
            // If the round is in RESULTS state, wipe the dealer hand
            if (table.getState() == GameState.RESULTS) {
                table.getDealer().getHand().clearHand();
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
                    // personalize snapshot for this player
                    snapshot = table.createSnapshotFor(p.getUsername());
                } else {
                    // dealer or unknown, no "you" flag
                    snapshot = table.createSnapshotFor(null);
                }

                Message update = new Message(
                        UUID.randomUUID().toString(),
                        MessageType.TABLE_SNAPSHOT,     
                        "SERVER",
                        handler.getClientID(),
                        snapshot,
                        java.time.LocalDateTime.now()
                );

                handler.sendMessage(update);
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

                // Auto start countdown for the first player that joins a table
        private void scheduleAutoStartIfNeeded(GameTable table) {
            // We only care about the very first player at this table
            int playerCount = table.getPlayers() != null ? table.getPlayers().size() : 0;
            if (playerCount != 1) {
                return;
            }

            String tableId = table.getTableID();
            System.out.println("[Server] First player joined table " + tableId
                    + ", starting 30 second auto start timer.");

            new Thread(() -> {
                try {
                    Thread.sleep(30_000);    // 30 seconds
                } catch (InterruptedException ignored) {
                }

                try {
                    // After 30 seconds, try to start the round on this table.
                    // This will only succeed if the table is still in a valid state
                    // and at least one player has a valid bet.
                    boolean started = table.startRound();
                    if (started) {
                        System.out.println("[Server] Auto starting round for table " + tableId);
                        // Do NOT broadcast here to avoid confusing the request-response flow.
                        // Players will see the new state when they call REQUEST_TABLE_STATE.
                    } else {
                        System.out.println("[Server] Auto start timer expired for table "
                                + tableId + ", but round did not start (maybe dealer started early, "
                                + "no players left, or no valid bets).");
                    }
                } catch (Exception e) {
                    System.err.println("[Server] Error in auto start timer for table "
                            + tableId + ": " + e.getMessage());
                }
            }, "AutoStart-" + tableId).start();
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

    } //end of Client Handler
    
     /* =========================
       Main entry point
       ========================= */    

    private static String findLocalIp() {
        try {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface ni : Collections.list(interfaces)) {
            if (!ni.isUp() || ni.isLoopback()) {
                continue;
            }
            for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr.getHostAddress();
                }
            }
        }
    } catch (SocketException e) {
        e.printStackTrace();
    }
    // fallback
    return "127.0.0.1";
}

    public static void main(String[] args) {
        manager = new LoginManager();
        System.out.println("Loading user data...");
        manager.loadData();
        System.out.println("User data loaded. Starting server.");

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
        ServerSocket server = null;
        int port = 8080;

        // NEW: Correct IP lookup
        String ip = findLocalIp();
        System.out.println("======================================");
        System.out.println("    Blackjack Server Started");
        System.out.println("    Local IP Address: " + ip);
        System.out.println("    Port: " + port);
        System.out.println("    Other players should connect to:");
        System.out.println("        " + ip + ":" + port);
        System.out.println("======================================");
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


 