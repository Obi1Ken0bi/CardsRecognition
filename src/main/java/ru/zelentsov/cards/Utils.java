package ru.zelentsov.cards;

import lombok.experimental.UtilityClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class Utils {
    public static List<BufferedImage> readCardsFromFile(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        BufferedImage deckImg = image.getSubimage(143, 586, 355, 87);
        List<BufferedImage> cards = new ArrayList<>(5);
        Map<Integer, Integer> positionForCard = new HashMap<>();
        positionForCard.put(0, 0);
        positionForCard.put(1, 72);
        positionForCard.put(2, 144);
        positionForCard.put(3, 215);
        positionForCard.put(4, 287);
        for (int i = 0; i < 5; i++) {
            //third card has other width
            int w = i == 2 ? 63 : 64;
            cards.add(deckImg.getSubimage(positionForCard.get(i), 0, w, 87));
        }
        return cards;
    }

    public static Boolean[][] getPixels(BufferedImage card) {
        int w = 26;
        int h = 24;
        BufferedImage symbolImageArea = card.getSubimage(6, 5, w, h);
        Boolean[][] pixels = new Boolean[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int pixel = symbolImageArea.getRGB(i, j);
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;
                boolean notWhite = red != 255 || green != 255 || blue != 255;
                boolean notGray = red != 120 || green != 120 || blue != 120;
                boolean isSymbol = notWhite && notGray;
                pixels[i][j] = isSymbol;
            }
        }
        return pixels;
    }
}
