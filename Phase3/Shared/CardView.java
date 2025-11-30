package Shared;

import java.io.Serializable;
import Enums.Rank;
import Enums.Suit;

public class CardView implements Serializable {
    private Rank rank;
    private Suit suit;
    private boolean hidden;

    public CardView(Rank rank, Suit suit, boolean hidden) {
        this.rank = rank;
        this.suit = suit;
        this.hidden = hidden;
    }

    public Rank getRank() { return rank; }
    public Suit getSuit() { return suit; }
    public boolean isHidden() { return hidden; }
}
