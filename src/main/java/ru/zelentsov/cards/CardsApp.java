package ru.zelentsov.cards;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.zelentsov.cards.CardScanner.isEmpty;

public class CardsApp {

    static ObjectMapper objectMapper = new ObjectMapper();
    static List<Symbol> symbols;

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new RuntimeException("specify path to folder");
            }
            readAllCardsFromDirectory(String.join(" ", args));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private static void readAllCardsFromDirectory(String path) throws IOException {
        File folder = new File(path);
        File[] files = folder.listFiles();
        for (File file : files) {
            String result = getAllCards(file.getPath());
            String fileName = file.getName();
            System.out.println(fileName + " - " + result);
        }
    }

    static String getAllCards(String path) throws IOException {
        symbols = objectMapper.readValue(new File("ready_model.json"), new TypeReference<>() {});
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File " + path + " does not exist");
            System.exit(1);
        }
        List<BufferedImage> cards = CardScanner.readCardsFromFile(file);
        return cards.stream().map(CardsApp::resolveCard).collect(Collectors.joining());
    }

    static String resolveCard(BufferedImage card) {
        if (isEmpty(card)) return "";

        return resolveValue(card) + resolveSuit(card);

    }

    private static String resolveValue(BufferedImage card) {
        Boolean[][] pixels = CardScanner.getPixels(card);
        int minCount = Integer.MAX_VALUE;
        int minId = -1;
        for (int i = 0; i < symbols.size(); i++) {
            Symbol symbol1 = symbols.get(i);
            int count = 0;
            Boolean[][] possiblePixels = symbol1.getPossiblePixels();
            for (int i1 = 0; i1 < possiblePixels.length; i1++) {
                for (int j = 0; j < possiblePixels[i1].length; j++) {
                    if (possiblePixels[i1][j] != pixels[i1][j]) {
                        count++;
                    }
                }
            }
            if (count < minCount) {
                minCount = count;
                minId = i;
            }
        }
        return symbols.get(minId).getValue();
    }

    private static String resolveSuit(BufferedImage card) {
        int suitCenter = card.getRGB(40, 65);
        int red = (suitCenter >> 16) & 0xFF;
        if (red <= 95) {
            int testPoint = card.getRGB(48, 59);
            int blue = testPoint & 0xFF;
            return blue > 116 ? "c" : "s";
        } else {
            int testPoint = card.getRGB(50, 55);
            int blue = testPoint & 0xFF;
            return blue > 116 ? "d" : "h";
        }
    }
}

