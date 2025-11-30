package Client;

public class MainApp {
    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.displayLoginScreen();       // or some startup screen
        gui.displayLobby();             // wait for user login
        // after login:
        gui.displayTable();             // show blackjack table UI
        
        // Example: loop for player sessions
        while (true) {
            gui.updateGameState();      // initialize or refresh game state
            gui.showPlayerOptions();    // show Hit / Stand / Bet options
            
            // wait for user action (depending on how GUI handles input)
            // after action:
            gui.showDealerOptions();    // when it's dealer turn
            gui.updateGameState();
            
            // at the end of the hand:
            gui.displayResult();        // (you may need to implement this)
            
            if (!gui.askPlayAgain()) {
                break;
            }
        }
        
        gui.shutdown();  // clean up, close sockets/windows
    }
}
