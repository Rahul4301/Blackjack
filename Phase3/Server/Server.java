package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    // CRITICAL FIX: LoginManager must be initialized ONCE to manage all accounts globally.
    private static LoginManager manager;
    // Use an ExecutorService for managing threads cleanly
    private static final int MAX_THREADS = 100;

    public static void main(String[] args) {
        // Initialize the account manager and load data before any clients connect
        manager = new LoginManager();
        System.out.println("Loading user data...");
        manager.loadData(); // Load all user accounts once
        System.out.println("User data loaded. Starting server.");

        // Thread pool to handle concurrent client connections
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

        ServerSocket server = null;
        int port = 8080; 

        System.out.println("ServerSocket awaiting connections on port " + port + "...");

        try {
            server = new ServerSocket(port);
            server.setReuseAddress(true);

            while (true) {
                // Blocks until a client connects
                Socket client = server.accept();
                
                System.out.print("\nNew client connected: " + client.getInetAddress().getHostAddress() + "\n");
                
                // Pass the single, shared LoginManager instance to the new client handler
                ClientHandler clientSock = new ClientHandler(client, manager);
                
                // Submit the handler to the thread pool for asynchronous execution
                pool.execute(clientSock);
            }
        } catch (IOException e) {
            System.err.println("Server encountered an I/O error: " + e.getMessage());
        } finally {
            if (server != null) {
                try {
                    server.close();
                    System.out.println("Server socket closed. Shutting down LoginManager.");
                    // Save data before final exit
                    manager.save();
                    pool.shutdown();
                } catch (IOException e) {
                    System.err.println("Error closing server: " + e.getMessage());
                }
            }
        }
    }
}




    //public void start();
    //public void stop();
    //public void broadcastUpdate();
    //public void handleMessage(Message msg);

