package ru.yandex.practicum;

import ru.yandex.practicum.gameException.LogWriteException;
import ru.yandex.practicum.gameException.WordNotFoundInDictionary;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {
    public WordleGame(WordleDictionary dictionary, PrintWriter logWrite, Random random) {
        this.dictionary = dictionary;
        this.answer = randomAnswer(random);
        this.logWrite = logWrite;
    }

    public static final int WORD_LENGTH = 5; //Допустимая длина слова
    public static final int MAX_ATTEMPTS = 6; //Количество попыток

    private String answer; //Правильный ответ

    private int steps; //Количество попыток

    private final WordleDictionary dictionary; //Словарь слов

    private final PrintWriter logWrite;

    private boolean isWin = false;

    private final Set<Character> banned = new HashSet<>(); //Буквы которых нет
    private final Set<Character> required = new HashSet<>(); //Буквы, которые есть
    private char[] rightPos; //Массив хранящий правильные символы в правильном месте
    private final Set<String> issuedHints = new HashSet<>(); //Список подсказок которые уже выданы

    //Проверка и сравнения слов
    public String checkingWord(String userAnswer) throws LogWriteException, WordNotFoundInDictionary {
        if (!dictionary.contains(userAnswer)) {
            throw new WordNotFoundInDictionary();
        }
        if (steps < 0 || steps > MAX_ATTEMPTS) {
            throw new RuntimeException("Некорректное значение steps: " + steps);
        }
        if (rightPos == null || rightPos.length != answer.length()) {
            throw new RuntimeException("Некорректное состояние rightPos");
        }
        if (userAnswer.length() != answer.length()) {
            throw new RuntimeException("Некорректная длина userAnswer: " + userAnswer);
        }


        logWrite.printf("Попытка: %d\n", steps + 1);
        logWrite.printf("Ответ пользователя: %s, правильный ответ: %s\n", userAnswer, answer);
        if (logWrite.checkError()) {
            throw new LogWriteException(new IOException("Ошибка записи в лог"));
        }

        steps++;


        char[] userC = userAnswer.toCharArray();
        char[] answerC = answer.toCharArray();

        StringBuilder resultBuilder = new StringBuilder();

        for (int i = 0; i < userAnswer.length(); i++) {
            if (userC[i] == answerC[i]) {
                required.add(userC[i]);
                rightPos[i] = answerC[i];
                resultBuilder.append("+");
                continue;
            }
            boolean found = false;
            for (int j = 0; j < answer.length(); j++) {
                if (userC[i] == answerC[j]) {
                    required.add(userC[i]);
                    resultBuilder.append("^");
                    found = true;
                    break;
                }
            }
            if (!found) {
                banned.add(userC[i]);
                resultBuilder.append("-");
            }
        }


        if (answer.equals(userAnswer.toLowerCase())) {
            isWin = true;
        }
        return resultBuilder.toString();
    }

    //Проверка на совпадение перед обработкой слова
    public boolean checkStartAnswer(String userAnswer) {
        if (answer.equals(userAnswer)) {
            isWin = true;
            return true;
        } else {
            return false;
        }
    }

    public String getHints(Random random) throws LogWriteException {
        if (rightPos == null) {
            throw new RuntimeException("rightPos не инициализирован");
        }

        List<String> hints = new ArrayList<>(dictionary.getAll());

        logWrite.println("Запрошена подсказка\n");
        if (logWrite.checkError()) {
            throw new LogWriteException(new IOException("Ошибка записи в лог"));
        }

        while (true) {
            boolean removed = false;
            Iterator<String> iterator = hints.iterator();

            while (iterator.hasNext()) {
                String str = iterator.next();

                // Проверка 1: запрещенные буквы
                for (char c : banned) {
                    if (str.indexOf(c) >= 0) {
                        iterator.remove();
                        removed = true;
                        break;
                    }
                }
                if (removed) continue;

                // Проверка 2: требуемые буквы
                for (char c : required) {
                    if (str.indexOf(c) < 0) {
                        iterator.remove();
                        removed = true;
                        break;
                    }
                }
                if (removed) continue;

                // Проверка 3: правильные позиции
                for (int i = 0; i < rightPos.length; i++) {
                    if (rightPos[i] != '*' && str.charAt(i) != rightPos[i]) {
                        iterator.remove();
                        removed = true;
                        break;
                    }
                }
            }

            if (!removed) break; // если ничего не удалили, фильтрация завершена
        }

        List<String> available = new ArrayList<>(); //Временный список

        //Добавление подсказок которых еще не было
        for (String word : hints) {
            if (!issuedHints.contains(word)) {
                available.add(word);
            }
        }

        if (available.isEmpty()) {
            return "Подсказок нет";
        }

        String result = available.get(random.nextInt(available.size()));
        issuedHints.add(result);

        logWrite.printf("Выдана подсказка: %s\n", result);
        if (logWrite.checkError()) {
            throw new LogWriteException(new IOException("Ошибка записи в лог"));
        }
        return result;
    }

    //Генерация правильного ответа
    public String randomAnswer(Random random) {
        int index = random.nextInt(dictionary.size());
        rightPos = new char[dictionary.get(index).length()];
        Arrays.fill(rightPos, '*'); //Заполнение массива *
        return dictionary.get(index);
    }

    public void newAnswer(Random random) {
        answer = dictionary.get(random.nextInt(dictionary.size()));
        steps = 0;
        banned.clear();
        required.clear();
        Arrays.fill(rightPos, '*');
        issuedHints.clear();
        isWin = false;
    }

    //Геттер победы
    public boolean getIsWin() {
        return isWin;
    }

    //Геттер ответа
    public String getAnswer() {
        return answer;
    }

    //Геттер попыток
    public int getSteps() {
        return steps;
    }


}
