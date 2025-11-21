

import enums.Rank;
import enums.Suit;

/**
 * Simple Card skeleton.
 */
public class Card {
    private Rank rank;
    private Suit suit;
    private int value;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        this.value = determineValue(rank);
    }

    private int determineValue(Rank rank) {
        switch (rank) {
            case TWO:
                return 2;
            case THREE:
                return 3;
            case FOUR:
                return 4;
            case FIVE:
                return 5;
            case SIX:
                return 6;
            case SEVEN:
                return 7;
            case EIGHT:
                return 8;
            case NINE:
                return 9;
            case TEN:
            case JACK:
            case QUEEN:
            case KING:
                return 10;
            case ACE:
            default:
                return 11; // default treat ACE as 11; game logic can adjust
        }
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return rank.name() + " of " + suit.name();
    }
}
