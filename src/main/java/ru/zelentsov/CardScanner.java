package ru.zelentsov;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CardScanner {
    private static final String TRAIN_SET_PATH = "src/main/resources/train_set";
    static List<Symbol> symbols = new ArrayList<>(14);

    public static void main(String[] args) throws IOException {
        test();
        System.out.println("end");
        for (int i = 0; i < symbols.size(); i++) {
            Symbol symbol = symbols.get(i);
            BufferedImage bufferedImage = new BufferedImage(26,24,BufferedImage.TYPE_INT_ARGB);
            for (int j = 0; j < symbol.getPossiblePixels().length; j++) {
                for (int i1 = 0; i1 < symbol.getPossiblePixels()[j].length; i1++) {
                    Boolean aBoolean = symbol.getPossiblePixels()[j][i1];
                    if (aBoolean) {
                        bufferedImage.setRGB(j,i1,Color.BLACK.getRGB());
                    }else
                    {
                        bufferedImage.setRGB(j,i1,Color.WHITE.getRGB());
                    }
                }
            }
            File file = new File("symbol " + i + ".png");
            try {
                ImageIO.write(bufferedImage, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writer().writeValue(new File("model.json"), symbols);
    }

    private static void test() {
        File folder = new File(TRAIN_SET_PATH);
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String result = getAllCards(file.getPath());
            System.out.println(result);
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
        if (!isCard) {
            return "";
        }

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
                if ((red == 255 && green == 255 && blue == 255) || (red == 120 && green == 120 && blue == 120)) {
                    pixels[i][j] = false;
                } else {
                    pixels[i][j] = true;
                }
            }
        }
        boolean alreadyExists = false;
        for (int i = 0; i < symbols.size(); i++) {
            Symbol symbol1 = symbols.get(i);
            Boolean[][] symbol2DimArray = symbol1.getPossiblePixels();
            int count = 0;
            for (int i1 = 0; i1 < symbol2DimArray.length; i1++) {
                for (int j = 0; j < symbol2DimArray[i1].length; j++) {
                    if (symbol2DimArray[i1][j] != pixels[i1][j]) {
                        count++;
                    }
                }
            }
            alreadyExists = count < 60;
            if (alreadyExists) {
                break;
            }
        }
        if (!alreadyExists) {
            symbols.add(new Symbol(pixels));
        }
        return "";
    }


    private static String resolveSuit(BufferedImage card) {
        int suitCenter = card.getRGB(40, 65);
        int red = (suitCenter >> 16) & 0xFF;
        if (red <= 95) {
            int testPoint = card.getRGB(48, 59);
            int blue = testPoint & 0xFF;
            if (blue > 116) return "c";
            return "s";
        } else {
            int testPoint = card.getRGB(50, 55);
            int blue = testPoint & 0xFF;
            if (blue > 116) return "d";
            return "h";
        }
    }

    private static void outPutCards(List<BufferedImage> cards) {
        cards.forEach(card -> {
            try {
                outputCard(card, "card_" + (cards.indexOf(card) + 1) + ".png");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void outputCard(BufferedImage card, String cards) throws IOException {
        ImageIO.write(card, "png", new File(cards));
    }
}

