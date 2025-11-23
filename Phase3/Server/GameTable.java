package Server;

import java.util.ArrayList;
import enums.GameState;

public class GameTable {
    private static int count;

    private int tableID;
    Dealer dealer;
    ArrayList<Player> players;
    Shoe shoe;
    ArrayList<Bet> bets;
    GameState state;

    public GameTable(Dealer dealer){
        tableID = ++count;
        this.dealer = dealer;
        players = new ArrayList<>(7);
        shoe = new Shoe(7);
        bets = new ArrayList<>();
        state = GameState.IN_PROGRESS;
    }

    public boolean addPlayer(Player player){
        if(players.size() < 7) {
            players.add(player);
            return true;
        }
        return false;
    }

    public void removePlayer(Player player){
        
    }
}
