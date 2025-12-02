package Client;

import javax.swing.*;
import java.net.Socket;

public class MainApp {

    public static void main(String[] args) {

        // Ask for mode
        String[] options = {"GUI Mode", "Console Mode"};
        int mode = JOptionPane.showOptionDialog(
                null,
                "Select a mode:",
                "Blackjack Launcher",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

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
                    "Invalid input. Using default port 8080.");
        }

        if (mode == 1) {
            // ------------------ CONSOLE MODE ------------------
            try {
                Socket socket = new Socket("localhost", port);

                java.io.ObjectOutputStream out =
                        new java.io.ObjectOutputStream(socket.getOutputStream());
                out.flush();
                java.io.ObjectInputStream in =
                        new java.io.ObjectInputStream(socket.getInputStream());

                Client client = new Client(out, in);
                Menu menu = new Menu(client);
                menu.displayMainMenu();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Console mode failed to start.\nError: " + e.getMessage());
            }
            return;
        }

        // ------------------ GUI MODE ------------------
        try {
            Socket socket = new Socket("localhost", port);

            java.io.ObjectOutputStream out =
                    new java.io.ObjectOutputStream(socket.getOutputStream());
            out.flush();
            java.io.ObjectInputStream in =
                    new java.io.ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to server on port " + port);

            // Create client for GUI
            Client client = new Client(out, in);

            // Launch GUI
            GUI gui = new GUI(client);
            client.setGUI(gui);

        } catch (Exception e) {

            // If connection fails, demo mode
            JOptionPane.showMessageDialog(
                    null,
                    "Could not connect to server.\nRunning in DEMO MODE.\n\nError: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.WARNING_MESSAGE
            );

            new GUI(null); // GUI demo mode
        }
    }
}
