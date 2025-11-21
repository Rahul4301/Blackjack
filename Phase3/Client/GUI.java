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
}
