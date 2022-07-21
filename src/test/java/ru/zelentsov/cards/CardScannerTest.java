package ru.zelentsov.cards;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class CardScannerTest {

    private static final String PATH_TO_EXAMPLES = "src/test/resources/examples";

    CardsApp cardsApp = new CardsApp();

    @SneakyThrows
    @Test
    void testOnAllCards() {
        File folder = new File(PATH_TO_EXAMPLES);
        File[] files = folder.listFiles();
        for (File file : files) {
            String result = cardsApp.getAllCards(file.getPath());
            String name = file.getName();
            name = name.replace(".png", "");
            for (int j = 0; j < result.length(); j++) {
                Assertions.assertEquals(result.charAt(j), name.charAt(j));
            }
        }
    }
}