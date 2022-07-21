package ru.zelentsov.cards;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class CardScannerTest {

    private static final String PATH_TO_EXAMPLES = "src/test/resources/examples";

    @SneakyThrows
    @Test
    void testOnAllCards() {
        CardsApp.symbols = new ObjectMapper().readValue(CardScannerTest.class.getClassLoader().getResourceAsStream("ready_model.json"), new TypeReference<>() {
        });
        File folder = new File(PATH_TO_EXAMPLES);
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            String result = CardsApp.getAllCards(file.getAbsolutePath());
            String name = file.getName();
            name = name.replace(".png", "");
            for (int j = 0; j < result.length(); j++) {
                Assertions.assertEquals(result.charAt(j), name.charAt(j));
            }
        }
    }
}