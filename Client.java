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
    public void recieveMessage() {
        return new Message("RESP_001", MessageType.OK, "Server", clientID, null, java.time.LocalDateTime.now());
    }
    public void disconnect() {
        System.out.println("Disconnected");
    }

}
