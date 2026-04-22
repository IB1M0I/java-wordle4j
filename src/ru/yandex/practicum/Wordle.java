package ru.yandex.practicum;

import ru.yandex.practicum.gameException.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

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
    private static final Random random = new Random();
    private static boolean isStart = true;

    public static void main(String[] args) {
        try {
            if (!Files.exists(log)) {
                try {
                    Files.createFile(log);
                    try (FileWriter fw = new FileWriter(log.toFile(), true);) {


                        fw.write("Файл log.txt создан");
                        fw.flush();
                    } catch (IOException e) {
                        System.out.printf("Ошибка создания log.txt: %s\n", e.getMessage());
                    }
                } catch (IOException e) {
                    System.out.printf("Ошибка поиска файла: %s\n", e.getMessage());
                }
            }

            try (Scanner scanner = new Scanner(System.in); PrintWriter logWrite = new PrintWriter(new FileWriter(log.toFile(), StandardCharsets.UTF_8), true);) {
                WordleDictionaryLoader wordleLoader = new WordleDictionaryLoader(logWrite);
                WordleDictionary dictionary = wordleLoader.getList(); //Получения отсортированного словаря
                WordleGame game = new WordleGame(dictionary, logWrite, random); //Создание объекта игры
                String userAnswer = ""; //Ответ игрока

                logWrite.println("Игра запущена");
                while (isStart) {
                    System.out.printf("Слово из %d букв загадано!\n", WordleGame.WORD_LENGTH);
                    logWrite.printf("Слово загадано: %s\n", game.getAnswer());
                    if (logWrite.checkError()) {
                        throw new LogWriteException(new IOException("Ошибка записи в лог"));
                    }

                    while (game.getSteps() < 6 && !game.getIsWin()) {
                        try {


                            System.out.println("Введите слово: ");
                            userAnswer = scanner.nextLine().toLowerCase().replace("ё", "е").trim();

                            if (userAnswer.isBlank()) {
                                System.out.println(game.getHints(random));
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
                    for (int i = 0; i < 4; i++) {
                        logWrite.println();
                    }
                    if (logWrite.checkError()) {
                        throw new LogWriteException(new IOException("Ошибка записи в лог"));
                    }

                    System.out.println("Сыграть еще раз? Да или Нет");
                    String command = scanner.nextLine().toLowerCase().trim();

                    if (command.equals("да")) {
                        System.out.println("Начало новой игры");
                        game.newAnswer(random);
                    } else {
                        System.out.println("========Игра окончена========");
                    }
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
        String answer = userAnswer.toLowerCase().replace("ё", "е").trim();
        if (answer.length() != WordleGame.WORD_LENGTH) {
            if (answer.length() > WordleGame.WORD_LENGTH) {
                logWrite.printf("ОШИБКА: ответ длинее %d символов\n", WordleGame.WORD_LENGTH);
            } else {
                logWrite.printf("ОШИБКА: ответ короче %d символов\n", WordleGame.WORD_LENGTH);
            }
            if (logWrite.checkError()) {
                throw new LogWriteException(new IOException("Ошибка записи в лог"));
            }
            throw new InvalidWordLengthException();
        } else if (!isRussian(answer)) {
            logWrite.println("ОШИБКА: ответ не на русском");
            if (logWrite.checkError()) {
                throw new LogWriteException(new IOException("Ошибка записи в лог"));
            }
            throw new NonRussianWordException();
        } else {
            logWrite.printf("Введен корректный ответ: %s\n", answer);
            if (logWrite.checkError()) {
                throw new LogWriteException(new IOException("Ошибка записи в лог"));
            }
        }
    }

    //Проверка на русские символы
    public static boolean isRussian(String userAnswer) {
        if (userAnswer == null) return false;
        return Pattern.compile("[а-я]{5}").matcher(userAnswer.toLowerCase()).matches();

    }


}
