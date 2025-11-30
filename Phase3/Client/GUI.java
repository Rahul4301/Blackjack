package Client;
public class GUI {
    private String currentScreen;

    public GUI() {
        this.currentScreen = "LOGIN";
    }

    public void displayLoginScreen() {
        System.out.println("Login Screen");
    }

    public void displayLobby() {
        System.out.println("Game Lobby");

    }

    public void displayTable() {
        System.out.println("BlackJack Table");

    }

    public void updateGameState() {
        System.out.println("Updating game state");

    }

    public void showPlayerOptions() {
        System.out.println("Player options: Hit, Stand, Double, Split");
    }

    public void showDealerOptions() {
        System.out.println("Dealer options");
    }

    public void showAnimations(){
        System.out.println("Showing animations");

    }

    public void displayError(String message) {
        System.out.println("ERROR: " + message);
    }

    public void displayResult() {
        System.out.println("Displaying game result");
    }

    public boolean askPlayAgain() {
        System.out.println("Asking player if they want to play again");
        return false; // Default: don't play again
    }

    public void shutdown() {
        System.out.println("Shutting down GUI and closing connections");
    }
}
