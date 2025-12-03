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
        String ip = JOptionPane.showInputDialog(
        null,
        "Enter server IP:",
        "Connect to Server",
        JOptionPane.QUESTION_MESSAGE
        );

        // User canceled the dialog
        if (ip == null) return;

        String portStr = JOptionPane.showInputDialog(
                null,
                "Enter server port:",
                "Connect to Server",
                JOptionPane.QUESTION_MESSAGE
        );

        // User canceled the dialog
        if (portStr == null) return;

        int port;
        try {
            port = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Port must be a number.",
                    "Invalid Port",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }


        if (mode == 1) {
            // ------------------ CONSOLE MODE ------------------
            try {
                Socket socket = new Socket("192.168.56.1", port);

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
            Socket socket = new Socket(ip, port);

            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream());
            out.flush();
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to server on port " + port);

            // Create client for GUI
            Client client = new Client(out, in);

            // Launch GUI
            GUI gui = new GUI(client);
            client.setGUI(gui);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Could not connect to server.\nCheck if server is on\n\nError: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }
    }
}