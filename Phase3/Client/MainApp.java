package Client;

import javax.swing.*;
import java.net.Socket;

public class MainApp {
    public static void main(String[] args) {
        // Ask for port
        String portStr = JOptionPane.showInputDialog(
            "Enter server port:", 
            "8080"
        );
        int port = 8080;
        try {
            port = Integer.parseInt(portStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Using default port 1234");
        }
        
        try {
            // Connect to server
            Socket socket = new Socket("localhost", port);
            java.io.ObjectOutputStream out = 
                new java.io.ObjectOutputStream(socket.getOutputStream());
            out.flush();
            java.io.ObjectInputStream in = 
                new java.io.ObjectInputStream(socket.getInputStream());
            
            System.out.println("Connected to server on port " + port);
            
            // Create client
            Client client = new Client(out, in);
            
            // Create GUI with client
            GUI gui = new GUI(client);
            
            // Connect GUI to client
            client.setGUI(gui);
            
        } catch (Exception e) {
            // If connection fails, run in demo mode
            JOptionPane.showMessageDialog(null,
                "Could not connect to server. Running in demo mode.\n" +
                "Error: " + e.getMessage(),
                "Connection Error",
                JOptionPane.WARNING_MESSAGE);
            
            // Create GUI with null client (demo mode)
            GUI gui = new GUI(null);
        }
    }
}