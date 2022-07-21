package ru.zelentsov.cards;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.zelentsov.cards.CardScanner.isEmpty;
import static ru.zelentsov.cards.Utils.getPixels;
import static ru.zelentsov.cards.Utils.readCardsFromFile;

public class CardsApp {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static List<Symbol> symbols;

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new RuntimeException("specify path to folder");
            }
            String path = args[0];
            readAllCardsFromDirectory(path);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private static void readAllCardsFromDirectory(String path) throws IOException {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) {
            symbols = objectMapper.readValue(CardsApp.class.getClassLoader().getResourceAsStream("ready_model.json"), new TypeReference<>() {
            });
            for (File file : files) {
                String result = getAllCards(file.getPath());
                String fileName = file.getName();
                System.out.println(fileName + " - " + result);
            }
        } else throw new RuntimeException("No files in folder.");
    }

    static String getAllCards(String path) throws IOException {
        File file = new File(path);
        List<BufferedImage> cards = readCardsFromFile(file);
        return cards.stream().map(CardsApp::resolveCard).collect(Collectors.joining());
    }

    static String resolveCard(BufferedImage card) {
        if (isEmpty(card)) return "";

        return resolveValue(card) + resolveSuit(card);
    }

    private static String resolveValue(BufferedImage card) {
        Boolean[][] pixels = getPixels(card);
        int minCount = Integer.MAX_VALUE;
        int minId = -1;
        for (int i = 0; i < symbols.size(); i++) {
            Symbol currentSymbol = symbols.get(i);
            int count = 0;
            Boolean[][] currentPixels = currentSymbol.getPixels();
            for (int k = 0; k < currentPixels.length; k++) {
                for (int j = 0; j < currentPixels[k].length; j++) {
                    if (currentPixels[k][j] != pixels[k][j]) {
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

