package ru.yandex.practicum;

import ru.yandex.practicum.gameException.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {
    private static final Path log = Paths.get("log.txt"); //Путь к логу

    public static void main(String[] args) {
        try {
            if (!Files.exists(log)) {
                try {
                    Files.createFile(log);
                    try (FileWriter fw = new FileWriter(log.toFile(), true);) {


                        fw.write("Файл log.txt создан");
                        fw.flush();
                    } catch (IOException e) {
                        System.out.printf("Ошибка создания log.txt: %s", e.getMessage());
                    }
                } catch (IOException e) {
                    System.out.printf("Ошибка поиска файла: %s", e.getMessage());
                }
            }

            try (Scanner scanner = new Scanner(System.in); PrintWriter logWrite = new PrintWriter(new FileWriter(log.toFile(), StandardCharsets.UTF_8), true);) {
                WordleDictionaryLoader wordleLoader = new WordleDictionaryLoader(logWrite);
                WordleDictionary dictionary = wordleLoader.getList(); //Получения отсортированного словоря
                WordleGame game = new WordleGame(dictionary, logWrite); //Создание объекта игры
                String userAnswer = ""; //Ответ игрока

                logWrite.println("Игра запущена");
                System.out.println("Слово из 5 букв загадано!");
                logWrite.printf("Слово загадано: %s\n", game.getAnswer());
                if (logWrite.checkError()) {
                    throw new LogWriteException(new IOException("Ошибка записи в лог"));
                }

                while (game.getSteps() < 6 && !game.getIsWin()) {
                    try {


                        System.out.println("Введите слово: ");
                        userAnswer = scanner.nextLine().toLowerCase().replace("ё", "е").trim();

                        if (userAnswer.isBlank()) {
                            System.out.println(game.getHints());
                            continue;
                        }

                        //Проверка коректного ответа
                        checkUserAnswer(userAnswer, logWrite);

                        //Проверка на совпадение сразу
                        if (game.checkStartAnswer(userAnswer)) {
                            System.out.println("Поздравляю! Слово отгадано");
                            break;
                        }

                        //Обработка ответа игрока
                        System.out.println(game.checkingWord(userAnswer));


                        if (game.getIsWin()) {
                            System.out.println("Поздравляю, слово отгадано!\n");
                            logWrite.printf("Игра выиграна! Попыток: %d\n", game.getSteps());
                            if (logWrite.checkError()) {
                                throw new LogWriteException(new IOException("Ошибка записи в лог"));
                            }
                        }
                    } catch (InvalidWordLengthException e) {
                        //Вывод сообщения об ошибке длины сообщения
                        System.out.println(e.getMessage());
                    } catch (NonRussianWordException e) {
                        //Вывод сообщения не на русском языке
                        System.out.println(e.getMessage());
                    } catch (WordNotFoundInDictionary e) {
                        System.out.println(e.getMessage());
                    }
                }

                if (!game.getIsWin()) {
                    System.out.println("К сожалению попытки закончились. Правильное слово: \n" + game.getAnswer());
                    logWrite.printf("Игра закончилась, правильное слово: %s\n", game.getAnswer());
                    if (logWrite.checkError()) {
                        throw new LogWriteException(new IOException("Ошибка записи в лог"));
                    }
                }

                logWrite.println("=========Игра окончена=========");
                for (int i = 0; i < 5; i++) {
                    logWrite.println();
                }
                if (logWrite.checkError()) {
                    throw new LogWriteException(new IOException("Ошибка записи в лог"));
                }


            } catch (LogWriteException | DictionaryLoadException e) {
                try (FileWriter logWriter = new FileWriter(log.toFile())) {
                    logWriter.write(String.format("%s  |||  %s", e.getMessage(), e.getCause().getMessage()));
                } catch (IOException ignored) {
                }
            } catch (IOException e) {
                System.out.println("Ошибка чтения/записи файла отчета");
            }

        } catch (Exception e) {
            System.out.println("Непредвиденная ошибка: " + e.getMessage());
        }

    }

    public static void checkUserAnswer(String userAnswer, PrintWriter logWrite) throws InvalidWordLengthException, NonRussianWordException, LogWriteException {
        if (userAnswer.length() != 5) {
            if (userAnswer.length() > 5) {
                logWrite.println("ОШИБКА: ответ длинее 5 символов");
            } else {
                logWrite.println("ОШИБКА: ответ короче 5 символов");
            }
            if (logWrite.checkError()) {
                throw new LogWriteException(new IOException("Ошибка записи в лог"));
            }
            throw new InvalidWordLengthException();
        } else if (!isRussian(userAnswer)) {
            logWrite.println("ОШИБКА: ответ не на русском");
            if (logWrite.checkError()) {
                throw new LogWriteException(new IOException("Ошибка записи в лог"));
            }
            throw new NonRussianWordException();
        } else {
            logWrite.printf("Введен корректный ответ: %s\n", userAnswer);
            if (logWrite.checkError()) {
                throw new LogWriteException(new IOException("Ошибка записи в лог"));
            }
        }
    }

    //Проверка на русские символы
    public static boolean isRussian(String userAnswer) {
        boolean isRus = true;
        for (char c : userAnswer.toCharArray()) {
            if (!((c >= 1040 && c <= 1071) || (c >= 1072 && c <= 1103))) {
                isRus = false;
                break;
            }
        }
        return isRus;
    }


}
