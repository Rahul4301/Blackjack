package Shared;

import java.io.Serializable;
import java.util.List;

public class PlayerView implements Serializable {
    private String playerUsername;
    private String username;
    private double betAmount;
    private int handValue;
    private boolean active;
    private boolean you;
    private boolean isYourTurn;
    private List<CardView> cards;

    public PlayerView(String playerUsername,
                      String username,
                      double betAmount,
                      int handValue,
                      boolean active,
                      boolean you,
                      boolean isYourTurn,
                      List<CardView> cards) {
        this.playerUsername = playerUsername;
        this.username = username;
        this.betAmount = betAmount;
        this.handValue = handValue;
        this.active = active;
        this.you = you;
        this.isYourTurn = isYourTurn;
        this.cards = cards;
    }

    public String getPlayerUsername() { return playerUsername; }
    public String getUsername() { return username; }
    public double getBetAmount() { return betAmount; }
    public int getHandValue() { return handValue; }
    public boolean isActive() { return active; }
    public boolean isYou() { return you; }
    public boolean isYourTurn() { return isYourTurn; }
    public List<CardView> getCards() { return cards; }
}
