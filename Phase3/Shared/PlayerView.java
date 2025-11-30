package Shared;

import java.io.Serializable;
import java.util.List;

public class PlayerView implements Serializable {
    private String playerId;
    private String username;
    private int betAmount;
    private int handValue;
    private boolean active;
    private boolean you;
    private List<CardView> cards;

    public PlayerView(String playerId,
                      String username,
                      int betAmount,
                      int handValue,
                      boolean active,
                      boolean you,
                      List<CardView> cards) {
        this.playerId = playerId;
        this.username = username;
        this.betAmount = betAmount;
        this.handValue = handValue;
        this.active = active;
        this.you = you;
        this.cards = cards;
    }

    public String getPlayerId() { return playerId; }
    public String getUsername() { return username; }
    public int getBetAmount() { return betAmount; }
    public int getHandValue() { return handValue; }
    public boolean isActive() { return active; }
    public boolean isYou() { return you; }
    public List<CardView> getCards() { return cards; }
}
