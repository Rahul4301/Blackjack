package Client;

import Enums.MessageType;
import Message.Message;
import Server.Account;
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

    public Account getAccount() {
        return account;
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


    //Table Messages
    // Table Messages
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
                // this.currentTableId = snapshot.getTableId();
                // this.currentSnapshot = snapshot;

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

    public static Message listTables() {
        return new Message(
                UUID.randomUUID().toString(),
                MessageType.LIST_TABLES,
                clientUUID,
                "SERVER",
                null,
                LocalDateTime.now()
        );
    }

    public static Message joinTable(String tableId) {
        return new Message(
                UUID.randomUUID().toString(),
                MessageType.JOIN_TABLE,
                clientUUID,
                "SERVER",
                tableId,
                LocalDateTime.now()
        );
    }

    public static Message leaveTable() {
        return new Message(
                UUID.randomUUID().toString(),
                MessageType.LEAVE_TABLE,
                clientUUID,
                "SERVER",
                null,
                LocalDateTime.now()
        );
    }

    //Snapshot helpers

    public void handleTableSnapshot(TableSnapshot snapshot) {
        this.currentSnapshot = snapshot;
        this.currentTableId = snapshot.getTableId();

        // For now, just print a simple view
        displaySnapshot(snapshot);
    }

    public void displaySnapshot(TableSnapshot snap) {
        System.out.println();
        System.out.println("=== Table " + snap.getTableId() + " ===");
        System.out.println("State: " + snap.getState());
        System.out.println("Current player id: " + snap.getCurrentPlayerId());

        // Dealer view
        DealerView dv = snap.getDealer();
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
            System.out.print("Player " + pv.getUsername() + " (id " + pv.getPlayerId() + ")");
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




    /* =========================
       Main entry point
       ========================= */

    public static void main(String[] args) {
        clientUUID = UUID.randomUUID().toString();
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter port #: ");
        int port = sc.nextInt();
        sc.nextLine(); // flush

        try (Socket socket = new Socket("localhost", port)) {

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
