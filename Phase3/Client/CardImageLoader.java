package Client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CardImageLoader {

    private static final int CARD_WIDTH = 167;
    private static final int CARD_HEIGHT = 243;

    private final Map<String, Image> cardImages = new HashMap<>();

    public CardImageLoader(String spritePath) {
        try {
            BufferedImage sheet = ImageIO.read(new File(spritePath));

            // 13 columns, 5 rows (4 suits + jokers/back row)
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 13; col++) {

                    // Cut card tile
                    BufferedImage sub = sheet.getSubimage(
                            col * CARD_WIDTH,
                            row * CARD_HEIGHT,
                            CARD_WIDTH,
                            CARD_HEIGHT
                    );

                    String key = makeKey(row, col);
                    if (key != null) {
                        cardImages.put(key, sub);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to load card sprites: " + e.getMessage());
        }
    }

    private String makeKey(int row, int col) {
        String[] ranks = {
                "A", "2", "3", "4", "5", "6", "7",
                "8", "9", "10", "J", "Q", "K"
        };

        String[] suits = {"CLUBS", "DIAMONDS", "HEARTS", "SPADES"};

        // Normal 52 cards
        if (row < 4 && col < 13) {
            return ranks[col] + "_" + suits[row];
        }

        // Row 4: jokers, back, misc
        if (row == 4) {
            if (col == 0) return "JOKER_RED";
            if (col == 1) return "JOKER_BLACK";
            if (col == 2) return "BACK";
        }

        return null;
    }

    public Image getCard(String rank, String suit) {
        return cardImages.get(rank + "_" + suit);
    }

    public Image getBack() {
        return cardImages.get("BACK");
    }
}
