package Shared;

import java.io.Serializable;
import java.util.List;

public class DealerView implements Serializable {
    private List<CardView> cards;
    private boolean hasHiddenCard;

    public DealerView(List<CardView> cards, boolean hasHiddenCard) {
        this.cards = cards;
        this.hasHiddenCard = hasHiddenCard;
    }

    public List<CardView> getCards() { return cards; }
    public boolean hasHiddenCard() { return hasHiddenCard; }
}
