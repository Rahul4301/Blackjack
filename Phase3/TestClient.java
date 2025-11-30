import Client.*;
import java.io.IOException;

/**
 * Quick integration test for GameClient and Menu
 */
public class TestClient {
    public static void main(String[] args) throws IOException {
        System.out.println("=== BLACKJACK CLIENT ===\n");
        
        // Create client (connects to localhost:8080 by default)
        GameClient client = new GameClient("localhost", 8080);
        
        try {
            // Test connection
            System.out.println("[*] Connecting to server...");
            if (!client.isConnected()) {
                System.err.println("[ERROR] Could not connect to server");
                System.exit(1);
            }
            System.out.println("[OK] Connected to server\n");
            
            // Create and start menu
            Menu menu = new Menu(client);
            System.out.println("[*] Starting main menu...\n");
            menu.displayMainMenu();
            
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
