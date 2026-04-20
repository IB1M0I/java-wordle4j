package ru.yandex.practicum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.gameException.DictionaryLoadException;
import ru.yandex.practicum.gameException.InvalidWordLengthException;
import ru.yandex.practicum.gameException.NonRussianWordException;
import ru.yandex.practicum.gameException.WordNotFoundInDictionary;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class WordleTest {
    static PrintWriter printWriter;
    static WordleGame game;

    @BeforeAll
    static void initAll() {
        printWriter = new PrintWriter(System.out, true);
    }

    @BeforeEach
    void initEach() {
        game = new WordleGame(new WordleDictionary(List.of("маска")), printWriter);
    }


    //Выброс InvalidWordLengthException при 4 символах
    @Test
    void checkUserAnswerShouldThrowForShortWord() {
        try {
            Wordle.checkUserAnswer("круг", printWriter);
            fail("Ожидался InvalidWordLengthException");
        } catch (InvalidWordLengthException e) {
            Assertions.assertEquals("ОШИБКА: ответ длинее/меньшн 5 символов", e.getMessage());
        }
    }

    //Выброс InvalidWordLengthException при 6 символах
    @Test
    void checkUserAnswerShouldThrowForLongWord() {
        try {
            Wordle.checkUserAnswer("каргуш", printWriter);
            fail("Ожидался InvalidWordLengthException");
        } catch (InvalidWordLengthException e) {
            Assertions.assertEquals("ОШИБКА: ответ длинее/меньшн 5 символов", e.getMessage());
        }
    }

    //Выброс NonRussianWordException с английскими символами
    @Test
    void checkUserAnswerShouldThrowForNonRussianInput() {
        try {
            Wordle.checkUserAnswer("apple", printWriter);
            fail("Ожидался NonRussianWordException");
        } catch (NonRussianWordException e) {
            Assertions.assertEquals("ОШИБКА: ответ содержит нерусские символы", e.getMessage());
        }
    }

    //Проверка, что корректный ответ не вызывает ошибок
    @Test
    void checkUserAnswerShouldPassForValidRussianWord() {
        try {
            Wordle.checkUserAnswer("Маска", printWriter);
        } catch (Exception e) {
            fail("Не ожидалось исключений, но появилось: " + e.getMessage());
        }
    }

    //Слово вне словаря кидает исключение WordNotFoundInDictionary
    @Test
    void checkingWordShouldThrowWhenWordIsNotInDictionary() {
        try {
            game.checkingWord("вапро");
            fail("Ожидался WordNotFoundInDictionary");
        } catch (WordNotFoundInDictionary e) {
            assertEquals("Слово не входит в список", e.getMessage());
        }

    }

    //Проверка на правильное совпадение
    @Test
    void checkStartAnswerShouldSetWinWhenAnswerMatches() {
        game = new WordleGame(new WordleDictionary(List.of("маска")), printWriter);
        Assertions.assertTrue(game.checkStartAnswer("маска"));
        Assertions.assertTrue(game.getIsWin());
    }

    //Проверка подсказок местонахождения букв
    @Test
    void checkingWordShouldReturnFiveCaretsAndIncreaseStepForCorrectWord() {

        String expected = "^^^^^"; //аванс

        Assertions.assertEquals(expected, game.checkingWord("маска"));
        Assertions.assertEquals(1, game.getSteps());
        Assertions.assertTrue(game.getIsWin());


    }

    //Проверка, что подсказки дважды не показываются
    @Test
    void getHintsShouldNotRepeatIssuedHint() {
        WordleDictionary dictionary = new WordleDictionary(List.of("абзац", "аванс"));
        game = new WordleGame(dictionary, printWriter);
        String hint1 = game.getHints();
        String hint2 = game.getHints();
        String hint3 = game.getHints();

        Assertions.assertNotEquals(hint1, hint2);
        Assertions.assertEquals("Подсказок нет", hint3);

    }

    @Test
    void getListShouldThrowDictionaryLoadExceptionWhenDictionaryIsEmpty() throws IOException {
        Path dict = Paths.get("words_ru.txt");
        Path backup = Paths.get("words_beckap_test.txt");

        Files.move(dict, backup, StandardCopyOption.REPLACE_EXISTING);
        Files.writeString(dict, "", StandardCharsets.UTF_8);

        try {
            WordleDictionaryLoader wordleDictionaryLoader = new WordleDictionaryLoader(printWriter);
            try {
                wordleDictionaryLoader.getList();
                fail("Ожидался DictionaryLoadException");
            } catch (DictionaryLoadException e) {
                assertEquals("Список не загружен", e.getMessage());
            }
        } finally {
            Files.deleteIfExists(dict);
            Files.move(backup, dict, StandardCopyOption.REPLACE_EXISTING);

        }
    }
}
