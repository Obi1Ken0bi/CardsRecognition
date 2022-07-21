package ru.zelentsov.cards;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardScanner {
    private static final String TRAIN_SET_PATH = "src/main/resources/train_set";
    private static final List<Symbol> symbols = new ArrayList<>(14);

    public static void main(String[] args) throws IOException {
        trainOnAllCards();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writer().writeValue(new File("src/main/resources/model.json"), symbols);
    }

    private static void trainOnAllCards() throws IOException {
        File folder = new File(TRAIN_SET_PATH);
        File[] files = folder.listFiles();
        for (File file : files) {
            trainOnExample(file.getPath());
        }
    }

    private static void trainOnExample(String path) throws IOException {
        File file = new File(path);
        List<BufferedImage> cards = readCardsFromFile(file);
        cards.forEach(CardScanner::trainCard);
    }

    static List<BufferedImage> readCardsFromFile(File file) throws IOException {
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

    private static void trainCard(BufferedImage card) {
        if (isEmpty(card)) return;
        trainValue(card);
    }

    static boolean isEmpty(BufferedImage card) {
        int rgb = card.getRGB(43, 11);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        boolean isCard = red > 119 && green > 119 && blue > 119;
        return !isCard;
    }

    private static void trainValue(BufferedImage card) {
        Boolean[][] pixels = getPixels(card);
        boolean alreadyExists = false;
        for (Symbol symbol1 : symbols) {
            Boolean[][] symbol2DimArray = symbol1.getPossiblePixels();
            int count = 0;
            for (int i1 = 0; i1 < symbol2DimArray.length; i1++)
                for (int j = 0; j < symbol2DimArray[i1].length; j++)
                    if (symbol2DimArray[i1][j] != pixels[i1][j]) count++;
            alreadyExists = count < 60;
            if (alreadyExists) break;
        }
        if (!alreadyExists) symbols.add(new Symbol(pixels));

    }

    static Boolean[][] getPixels(BufferedImage card) {
        BufferedImage symbol = card.getSubimage(6, 5, 26, 24);
        Boolean[][] pixels = new Boolean[26][24];
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 24; j++) {
                int rgb = symbol.getRGB(i, j);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                pixels[i][j] = (red != 255 || green != 255 || blue != 255) && (red != 120 || green != 120 || blue != 120);
            }
        }
        return pixels;
    }
}
