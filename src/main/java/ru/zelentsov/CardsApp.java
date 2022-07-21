package ru.zelentsov;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CardsApp {

    static ObjectMapper objectMapper = new ObjectMapper();
    static List<Symbol> symbols;

    static {
        try {
            symbols = objectMapper.readValue(new File("ready_model.json"), new TypeReference<List<Symbol>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        File folder = new File("/home/ubik33/Загрузки/Telegram Desktop/imgs_marked/imgs_marked");
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String result = getAllCards(file.getPath());
            String name = file.getName();
            name = name.replace(".png", "");
            for (int j = 0; j < result.length(); j++) {
                boolean b = result.charAt(j) == name.charAt(j);
                if (!b) {
                    System.out.println(file.getName() + " " + result.charAt(j) + " " + name.charAt((j)) + " " + j);
                }
            }

        }
    }

    private static String getAllCards(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File " + path + " does not exist");
            System.exit(1);
        }
        try {
            BufferedImage image = ImageIO.read(file);
            BufferedImage deckImg = image.getSubimage(143, 586, 355, 87);
            ImageIO.write(deckImg, "png", new File("deck.png"));
            List<BufferedImage> cards = new ArrayList<>(5);
            Map<Integer, Integer> positions = new HashMap<>();
            positions.put(0, 0);
            positions.put(1, 72);
            positions.put(2, 144);
            positions.put(3, 215);
            positions.put(4, 287);
            for (int i = 0; i < 5; i++) {
                cards.add(deckImg.getSubimage(positions.get(i), 0, i == 2 ? 63 : 64, 87));
            }
            outPutCards(cards);
            return cards.stream().map(card -> resolveCard(card)).collect(Collectors.joining());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String resolveCard(BufferedImage card) {
        int rgb = card.getRGB(43, 11);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        boolean isCard = red > 119 && green > 119 && blue > 119;
        if (!isCard) return "";

        return resolveValue(card) + resolveSuit(card);

    }

    private static String resolveValue(BufferedImage card) {
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

    private static void outPutCards(List<BufferedImage> cards) {
        cards.forEach(card -> {
            try {
                ImageIO.write(card, "png", new File("card_" + (cards.indexOf(card) + 1) + ".png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

class Symbol {
    private Boolean[][] possiblePixels;
    private String value;

    public Symbol() {
    }

    public Symbol(Boolean[][] possiblePixels) {
        this.possiblePixels = possiblePixels;
    }

    public Boolean[][] getPossiblePixels() {
        return possiblePixels;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
