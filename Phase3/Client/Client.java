package Client;

import Enums.MessageType;
import Message.Message;
import Server.Account;
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

    //Table Functions

    public void createTable() {
        try{
            Message createTableMsg =  new Message(
                UUID.randomUUID().toString(),
                MessageType.CREATE_TABLE,
                clientUUID,
                "SERVER",
                null,
                LocalDateTime.now()
            );

            out.writeObject(createTableMsg);
            out.flush();


        } catch (IOException e){
            System.out.println("Error sending CREATE_TABLE: " + e.getMessage());
        }
    }

    public void listTables() {
        try{
            Message createListTablesMsg = new Message(
                UUID.randomUUID().toString(),
                MessageType.LIST_TABLES,
                clientUUID,
                "SERVER",
                null,
                LocalDateTime.now()
            );

            out.writeObject(createListTablesMsg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending LIST_TABLE: " + e.getMessage());
        }
        
    }

    public void joinTable(String tableId) {
        try{
            Message joinTableMsg =  new Message(
                    UUID.randomUUID().toString(),
                    MessageType.JOIN_TABLE,
                    clientUUID,
                    "SERVER",
                    tableId,
                    LocalDateTime.now()
            );

            out.writeObject(joinTableMsg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending JOIN_TABLE: " + e.getMessage());
        }

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
