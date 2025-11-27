package Client;

import Enums.MessageType;
import Message.Message;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Client-side socket communication handler.
 * Manages connection to server, sending/receiving messages, and async message handling.
 */
public class Client {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected;
    private String clientID;
    private BlockingQueue<Message> messageQueue;
    private MessageListener messageListener;

    public Client() {
        this.clientID = UUID.randomUUID().toString();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.connected = false;
    }

    /**
     * Connect to server at the given address and port.
     * Starts a background listener thread to receive messages.
     */
    public boolean connect(String serverAddress, int serverPort) {
        try {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.socket = new Socket(serverAddress, serverPort);
            
            // Initialize streams (output first to avoid deadlock)
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
            
            this.connected = true;
            System.out.println("[Client] Connected to " + serverAddress + ":" + serverPort);

            // Start background listener thread
            startMessageListener();

            return true;
        } catch (IOException e) {
            System.err.println("[Client] Connection failed: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    /**
     * Send a message to the server.
     */
    public boolean sendMessage(Message msg) {
        if (!connected || out == null) {
            System.err.println("[Client] Not connected. Cannot send message.");
            return false;
        }

        try {
            synchronized (out) {
                out.writeObject(msg);
                out.flush();
            }
            System.out.println("[Client] Sent: " + msg);
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Failed to send message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Receive a message from the queue (non-blocking).
     * Returns null if no message is available.
     */
    public Message receiveMessage() {
        return messageQueue.poll();
    }

    /**
     * Receive a message from the queue (blocking).
     * Waits up to timeoutMs milliseconds for a message.
     */
    public Message receiveMessage(long timeoutMs) {
        try {
            return messageQueue.poll(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("[Client] Interrupted while waiting for message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Disconnect from server.
     */
    public void disconnect() {
        try {
            connected = false;
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[Client] Disconnected.");
        } catch (IOException e) {
            System.err.println("[Client] Error during disconnect: " + e.getMessage());
        }
    }

    /**
     * Check if client is connected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Get the unique client ID.
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * Set a custom message listener callback.
     */
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    /**
     * Start background thread to listen for incoming messages.
     */
    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            while (connected) {
                try {
                    Message msg = (Message) in.readObject();
                    messageQueue.offer(msg);
                    System.out.println("[Client] Received: " + msg);

                    // Invoke callback if set
                    if (messageListener != null) {
                        messageListener.onMessageReceived(msg);
                    }
                } catch (EOFException e) {
                    // Server closed connection gracefully
                    System.out.println("[Client] Server disconnected.");
                    connected = false;
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    if (connected) {
                        System.err.println("[Client] Error receiving message: " + e.getMessage());
                    }
                    connected = false;
                    break;
                }
            }
        }, "ClientMessageListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Helper method to send a simple message.
     */
    public boolean sendSimpleMessage(MessageType type, String recipient, Object payload) {
        Message msg = new Message(
            UUID.randomUUID().toString(),
            type,
            clientID,
            recipient,
            payload,
            LocalDateTime.now()
        );
        return sendMessage(msg);
    }

    /**
     * Callback interface for custom message handling.
     */
    public interface MessageListener {
        void onMessageReceived(Message msg);
    }
}
