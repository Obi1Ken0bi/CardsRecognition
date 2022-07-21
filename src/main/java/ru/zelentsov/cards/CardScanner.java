package ru.zelentsov.cards;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.zelentsov.cards.Utils.getPixels;
import static ru.zelentsov.cards.Utils.readCardsFromFile;

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
        if (files != null) {
            for (File file : files) {
                trainOnExample(file.getPath());
            }
        }
    }

    private static void trainOnExample(String path) throws IOException {
        File file = new File(path);
        List<BufferedImage> cards = readCardsFromFile(file);
        cards.forEach(CardScanner::trainCard);
    }

    private static void trainCard(BufferedImage card) {
        if (isEmpty(card)) return;
        trainValue(card);
    }

    private static void trainValue(BufferedImage card) {
        Boolean[][] pixels = getPixels(card);
        boolean alreadyExists = false;
        for (Symbol symbol1 : symbols) {
            Boolean[][] symbol2DimArray = symbol1.getPixels();
            int count = 0;
            for (int i1 = 0; i1 < symbol2DimArray.length; i1++)
                for (int j = 0; j < symbol2DimArray[i1].length; j++)
                    if (symbol2DimArray[i1][j] != pixels[i1][j]) count++;
            alreadyExists = count < 60;
            if (alreadyExists) break;
        }
        if (!alreadyExists) symbols.add(new Symbol(pixels));
    }

    public static boolean isEmpty(BufferedImage card) {
        int rgb = card.getRGB(43, 11);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        boolean isCard = red > 119 && green > 119 && blue > 119;
        return !isCard;
    }

}
