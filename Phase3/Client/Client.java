package Client;

import Enums.MessageType;
import Enums.PlayerAction;
import Message.Message;
import Server.Account;
import Server.Dealer;
import Shared.CardView;
import Shared.DealerView;
import Shared.PlayerView;
import Shared.TableSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.UUID;

public class Client {

    private static String clientUUID;

    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private boolean connected;
    private GUI gui;
    private Account account;
    
    private TableSnapshot currentSnapshot;
    private String currentTableId;


    public Client(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;
    }

    public boolean isLoggedIn() {
        return account != null;
    }
    public boolean isConnected() {
        return connected && out != null;
    }

    public Account getAccount() {
        return account;
    }
    public void setGUI(GUI gui) {
        this.gui = gui;
        if (gui != null) {
            gui.setClient(this);
        }
    }

    public GUI getGUI() {
        return gui;
    }
    /* =========================
       Client <-> Server actions
       ========================= */

    public boolean login(String username, String password) {
        try {
            if (account != null){
                throw new ClassNotFoundException("Already logged in!");
            }
            String[] userpw = { username, password };
            Message loginMsg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.LOGIN,
                    clientUUID,
                    "SERVER",
                    userpw,
                    LocalDateTime.now()
            );

            out.writeObject(loginMsg);
            out.flush();

            Message response = (Message) in.readObject();
            System.out.println(response.toString());

            if (response.getPayload() instanceof Account acc) {
                System.out.println(acc.getUsername() + " is logged in!");
                this.account = acc;
                return true;
            }

            return false;

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during login: " + e.getMessage());
            return false;
        }
    }

    public boolean register(String username, String password, String type) {
        try {
            String[] regData = { username, password, type };
            Message registerMsg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.REGISTER,
                    clientUUID,
                    "SERVER",
                    regData,
                    LocalDateTime.now()
            );

            out.writeObject(registerMsg);
            out.flush();

            Message response = (Message) in.readObject();
            System.out.println(response.toString());

            // if server returns an Account on successful register + auto login
            if (response.getPayload() instanceof Account acc) {
                System.out.println("Registered and logged in as " + acc.getUsername());
                this.account = acc;
                return true;
            }

            return false;

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during register: " + e.getMessage());
            return false;
        }
    }

    public void logout() {
        if (!isLoggedIn()) {
            System.out.println("Not logged in.");
            return;
        }

        try {
            Message logoutMsg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.LOGOUT,
                    clientUUID,
                    "SERVER",
                    null,
                    LocalDateTime.now()
            );

            out.writeObject(logoutMsg);
            out.flush();

            Message response = (Message) in.readObject();
            System.out.println(response.toString());

            account = null;

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during logout: " + e.getMessage());
        }
    }

    public void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
                System.out.println("[Client] Sent: " + msg.getMessageType());
            } else {
                System.err.println("[Client] Cannot send message: output stream is null");
            }
        } catch (IOException e) {
            System.err.println("[Client] Error sending message: " + e.getMessage());
            // Handle disconnect
        }
    }

    public void sendExit() {
            try {
                Message exitMsg = new Message(
                        UUID.randomUUID().toString(),
                        MessageType.EXIT,
                        clientUUID,
                        "SERVER",
                        null,
                        LocalDateTime.now()
                );

                out.writeObject(exitMsg);
                out.flush();

                Message response = (Message) in.readObject();
                System.out.println(response.toString());

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error sending EXIT: " + e.getMessage());
            }
        }

        public boolean deposit(double amount) {
        if (!isLoggedIn()) {
            System.out.println("Must be logged in to deposit");
            return false;
        }

        try {
            // Server expects a String payload for DEPOSIT
            String payload = Double.toString(amount);

            Message depositMsg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.DEPOSIT,
                    clientUUID,
                    "SERVER",
                    payload,
                    LocalDateTime.now()
            );

            out.writeObject(depositMsg);
            out.flush();

            Message response = (Message) in.readObject();

            if (response.getMessageType() == MessageType.OK) {
                System.out.println("Deposit OK: " + response.getPayload());

                // Optional: refresh table state so GUI can show new balance
                if (currentTableId != null) {
                    requestTableState();
                }
                return true;

            } else if (response.getMessageType() == MessageType.ERROR) {
                System.out.println("Deposit error: " + response.getPayload());
                return false;

            } else {
                System.out.println("Unexpected response to DEPOSIT: " + response);
                return false;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during deposit: " + e.getMessage());
            return false;
        }
    }



    //Table Messages
    public TableSnapshot createTable() {
        try {
            Message createTableMsg = new Message(
                UUID.randomUUID().toString(),
                MessageType.CREATE_TABLE,
                clientUUID,
                "SERVER",
                null,
                LocalDateTime.now()
            );

            out.writeObject(createTableMsg);
            out.flush();

            Message response = (Message) in.readObject();
            System.out.println("Response to CREATE_TABLE: " + response.getMessageType());

            // Handle error first
            if (response.getMessageType() == MessageType.ERROR) {
                System.out.println("Server error on CREATE_TABLE: " + response.getPayload());
                return null;
            }

            Object payload = response.getPayload();

            // Server's handleCreateTable sends OK with a TableSnapshot payload
            if (response.getMessageType() == MessageType.OK && payload instanceof TableSnapshot snapshot) {
                // You can also store it in a field if you want
                this.currentTableId = snapshot.getTableId();
                this.currentSnapshot = snapshot;

                return snapshot;
            }

            System.out.println(
                "Unexpected response to CREATE_TABLE: " +
                response.getMessageType() + " payload=" + payload
            );
            return null;

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error sending CREATE_TABLE: " + e.getMessage());
            return null;
        }
}

public TableSnapshot joinTable(String tableId) {
    try{
        Message joinMsg = new Message(
        UUID.randomUUID().toString(),
        MessageType.JOIN_TABLE,
        "CLIENT",
        "SERVER",
        tableId,  // The table ID to join
        LocalDateTime.now()
    );
        sendMessage(joinMsg);
        Message response = (Message) in.readObject();
        System.out.println(response.getPayload());

        if(response.getPayload() instanceof TableSnapshot tableSnapshot){
            currentTableId = tableId;
            return tableSnapshot;
        }
        throw new ClassNotFoundException("No table snapshot returned from server!");
    } catch (IOException | ClassNotFoundException e){
        System.out.println("Error sending JOIN_TABLE: " + e.getMessage());
        return null;
    }
}

public void leaveTable() {
    if (!isLoggedIn()) {
        System.out.println("Must be logged in");
        return;
    }

    try {
        Message leaveMsg = new Message(
                UUID.randomUUID().toString(),
                MessageType.LEAVE_TABLE,
                clientUUID,
                "SERVER",
                null,
                LocalDateTime.now()
        );

        out.writeObject(leaveMsg);
        out.flush();

        Message response = (Message) in.readObject();
        System.out.println(response.getPayload());

        if (response.getMessageType() == MessageType.OK) {
            // We successfully left whatever table the server thinks we were on
            currentTableId = null;
            currentSnapshot = null;
            return;
        }


    } catch (IOException | ClassNotFoundException e) {
        System.out.println("Error leaving table: " + e.getMessage());
    }
    }

    public void listTables() {
        if (!isLoggedIn()) {
            System.out.println("Must be logged in");
            return;
        }

        try {
            Message listMsg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.LIST_TABLES,
                    clientUUID,
                    "SERVER",
                    null,
                    LocalDateTime.now()
            );

            out.writeObject(listMsg);
            out.flush();

            Message response = (Message) in.readObject();
            
            if (response.getPayload() instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> tables = (java.util.List<String>) response.getPayload();
                System.out.println("Available tables:");
                for (String id : tables) {
                    System.out.println("  - " + id);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error listing tables: " + e.getMessage());
        }
    }
    public void placeBet(double amount) {
        if (!isLoggedIn()) {
            System.out.println("Must be logged in");
            return;
        }

        try {
            Message betMsg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.BET_PLACED,
                    clientUUID,
                    "SERVER",
                    amount,
                    LocalDateTime.now()
            );

            out.writeObject(betMsg);
            out.flush();

            Message response = (Message) in.readObject();
            
            if (response.getMessageType() == MessageType.TABLE_SNAPSHOT) {
                TableSnapshot snapshot = (TableSnapshot) response.getPayload();
                // Update GUI if needed
                System.out.println("Bet placed: $" + amount);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error placing bet: " + e.getMessage());
        }
    }

    public TableSnapshot sendPlayerAction(PlayerAction action) {
        if (!isLoggedIn()) {
            System.out.println("Must be logged in");
            return null;
            }

        try {
            Message actionMsg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.PLAYER_ACTION,
                    clientUUID,
                    "SERVER",
                    action,                    // send the enum, not a String
                    LocalDateTime.now()
            );

            out.writeObject(actionMsg);
            out.flush();

            Message response = (Message) in.readObject();

            if (response.getMessageType() == MessageType.TABLE_SNAPSHOT &&
                    response.getPayload() instanceof TableSnapshot snapshot) {

                return snapshot;

            } else if (response.getMessageType() == MessageType.ERROR) {
                System.out.println("Server error: " + response.getPayload());
                return null;
            } else {
                System.out.println("Unexpected response to PLAYER_ACTION: " + response);
                return null;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error sending action: " + e.getMessage());
            return null;
        }
    }




    public void handleTableSnapshot(TableSnapshot snapshot) {
        this.currentSnapshot = snapshot;
        this.currentTableId = snapshot.getTableId();

        displaySnapshot(snapshot);
    }

    public void displaySnapshot(TableSnapshot snap) {
        System.out.println();
        System.out.println("=== Table " + snap.getTableId() + " ===");
        System.out.println("State: " + snap.getState());
        System.out.println("Current player: " + snap.getCurrentPlayerUsername());

        // Dealer view
        DealerView dv = snap.getDealerView();
        System.out.print("Dealer cards: ");
        for (CardView cv : dv.getCards()) {
            if (cv.isHidden()) {
                System.out.print("[HIDDEN] ");
            } else {
                System.out.print(cv.getRank() + " of " + cv.getSuit() + " ");
            }
        }
        if (dv.hasHiddenCard()) {
            System.out.print("(has hidden card)");
        }
        System.out.println();

        // Each player
        for (PlayerView pv : snap.getPlayers()) {
            System.out.print("Player " + pv.getUsername());
            if (pv.isYou()) {
                System.out.print(" (YOU)");
            }
            if (pv.isYourTurn()) {
                System.out.print(" [TURN]");
            }
            System.out.println();

            System.out.print("  Hand: ");
            for (CardView cv : pv.getCards()) {
                System.out.print(cv.getRank() + " of " + cv.getSuit() + " ");
            }
            System.out.println();

            System.out.println("  Bet: " + pv.getBetAmount()
                            + " | handValue: " + pv.getHandValue()
                            + " | active: " + pv.isActive());
        }
    }

   public void startRound(){
        try{
            Message start = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.START,
                    clientUUID,
                    "SERVER",
                    null,
                    LocalDateTime.now()
            );

            out.writeObject(start);
            out.flush();

            Message response = (Message) in.readObject();
            if(!(response.getPayload() instanceof TableSnapshot)){
                System.out.println("A TableSnapshot was expected");
                return;
            }
            handleTableSnapshot((TableSnapshot) response.getPayload());
        } catch (IOException | ClassNotFoundException e){

        }
   }

    public TableSnapshot requestNextRound() {
        if (!isLoggedIn()) {
            System.out.println("Must be logged in");
            return null;
        }

        try {
            Message msg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.NEXT_ROUND,
                    clientUUID,
                    "SERVER",
                    null,
                    LocalDateTime.now()
            );

            out.writeObject(msg);
            out.flush();

            Message response = (Message) in.readObject();

            if (response.getMessageType() == MessageType.OK &&
                response.getPayload() instanceof TableSnapshot snapshot) {

                // Keep client-side state in sync
                handleTableSnapshot(snapshot);
                return snapshot;

            } else if (response.getMessageType() == MessageType.ERROR) {
                System.out.println("Error requesting next round: " + response.getPayload());
                return null;
            } else {
                System.out.println("Unexpected response to NEXT_ROUND: " + response);
                return null;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error sending NEXT_ROUND: " + e.getMessage());
            return null;
        }
    }




    //Snapshot helpers

    public TableSnapshot requestTableState() {
        try {
            Message req = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.REQUEST_TABLE_STATE,
                    clientUUID,
                    "SERVER",
                    null,
                    LocalDateTime.now()
            );

            out.writeObject(req);
            out.flush();

            Message response = (Message) in.readObject();

            if (response.getMessageType() == MessageType.ERROR) {
                System.out.println("Error requesting table state: " + response.getPayload());
                return null;
            }

            Object payload = response.getPayload();
            if (response.getMessageType() == MessageType.OK && payload instanceof TableSnapshot snapshot) {
                // You can reuse your existing helper
                handleTableSnapshot(snapshot);
                return snapshot;
            }

            System.out.println("Unexpected response to REQUEST_TABLE_STATE: " + response);
            return null;

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in requestTableState: " + e.getMessage());
            return null;
        }
    }

    public void playerHit(){
        try{
            Message playerHitMsg = new Message(
                UUID.randomUUID().toString(),
                MessageType.PLAYER_ACTION,
                clientUUID,
                "SERVER",
                PlayerAction.HIT,
                LocalDateTime.now()
            );

        sendMessage(playerHitMsg);

        Message response = (Message) in.readObject();

        if(response.getMessageType() == MessageType.OK){
            requestTableState();
            return;
        }
        throw new ClassNotFoundException("Error with playerHitMsg");
        } catch (IOException | ClassNotFoundException e){
            System.out.println(e);
        }
        
    }

    public java.util.List<String> requestTableList() {
        try {
            Message listMsg = new Message(
                    UUID.randomUUID().toString(),
                    MessageType.LIST_TABLES,
                    clientUUID,
                    "SERVER",
                    null,
                    LocalDateTime.now()
            );

            sendMessage(listMsg);

            Message response = (Message) in.readObject();

            if (response.getMessageType() == MessageType.OK &&
                response.getPayload() instanceof java.util.List<?> rawList) {

                java.util.List<String> result = new java.util.ArrayList<>();

                for (Object o : rawList) {
                    if (o instanceof String s) {
                        result.add(s);
                    }
                }

                return result;
            }

            return null;

        } catch (Exception e) {
            System.out.println("Error requesting table list: " + e.getMessage());
            return null;
        }
    }


    public String getUsername(){
        return account.getUsername();
    }

    public boolean isDealer() {
        return account instanceof Dealer;
    }


    /* =========================
       Main entry point
       ========================= */

    public static void main(String[] args) {
        clientUUID = UUID.randomUUID().toString();
        Scanner sc = new Scanner(System.in);

        // System.out.print("Enter port #: ");
        // int port = sc.nextInt();
        // sc.nextLine(); // flush

        try (Socket socket = new Socket("localhost", 8080)) {

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            System.out.println("\nConnection successful.");

            Client client = new Client(out, in);
            Menu menu = new Menu(client);

            // Hand all interaction to the menu.
            // Menu will internally call client.login(), client.logout(), etc.
            menu.displayMainMenu();

            // When the menu loop ends, send EXIT and close connection.
            client.sendExit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}