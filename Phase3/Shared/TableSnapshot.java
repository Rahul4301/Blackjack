package Shared;

import Enums.GameState;
import java.io.Serializable;
import java.util.List;

public class TableSnapshot implements Serializable {
    private String tableId;
    private GameState state;
    private String currentPlayerUsername;
    private DealerView dealer;
    private List<PlayerView> players;

    public TableSnapshot(String tableId,
                         GameState state,
                         String currentPlayerUsername,
                         DealerView dealer,
                         List<PlayerView> players) {
        this.tableId = tableId;
        this.state = state;
        this.currentPlayerUsername = currentPlayerUsername;
        this.dealer = dealer;
        this.players = players;
    }

    public String getTableId() { return tableId; }
    public GameState getState() { return state; }
    public String getCurrentPlayerUsername() { return currentPlayerUsername; }
    public DealerView getDealerView() { return dealer; }
    public List<PlayerView> getPlayers() { return players; }
}
