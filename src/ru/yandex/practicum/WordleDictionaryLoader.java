package ru.yandex.practicum;

import ru.yandex.practicum.gameException.DictionaryLoadException;
import ru.yandex.practicum.gameException.LogWriteException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {
    public WordleDictionaryLoader(PrintWriter logWrite) {
        this.logWrite = logWrite;
    }

    PrintWriter logWrite;

    public WordleDictionary getList() throws DictionaryLoadException, LogWriteException {
        List<String> words = new ArrayList<>(); //Список подходящих слов
        String line; //Прочитанное слово
        try (FileReader fileReader = new FileReader("words_ru.txt"); BufferedReader bufferedReader = new BufferedReader(fileReader)) {


            while (bufferedReader.ready()) {
                line = bufferedReader.readLine();
                if (line.length() == 5) {
                    String processedWord = line.toLowerCase().replace("ё", "е");
                    words.add(processedWord);
                }

            }

            if (words.isEmpty()) {
                logWrite.println("Не удалось заполнить список");
                if (logWrite.checkError()) {
                    throw new LogWriteException(new IOException("Ошибка записи в лог"));
                }
                throw new DictionaryLoadException(new Exception("Список пуст"));
            }
            logWrite.printf("Словарь обработан: %d\n", words.size());
            if (logWrite.checkError()) {
                throw new LogWriteException(new IOException("Ошибка записи в лог"));
            }
            return new WordleDictionary(words);

        } catch (IOException e) {
            throw new DictionaryLoadException(e); //Заменить
        }
    }

}
