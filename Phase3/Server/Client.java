package Server;

import enums.MessageType;
import Message.Message;

public class Client {
    private String clientID;

    public Client() {
        this.clientID = "CLIENT_" + System.currentTimeMillis();
    }

    public void connect (String serverAddr, int port) {
        System.out.println("Connected to " + serverAddr + ":" + port);

    }

    public void sendMessage(Message msg) {
        System.out.println("Sending: " + msg.getMessageType());
    }
    public Message recieveMessage() {
        return new Message(MessageType.OK, "Server", clientID, null);
    }
    public void disconnect() {
        System.out.println("Disconnected");
    }

}
