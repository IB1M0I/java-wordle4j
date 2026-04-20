package ru.yandex.practicum.gameException;

//Исключение слова нет в списке
public class WordNotFoundInDictionary extends RuntimeException {
    public WordNotFoundInDictionary() {
        super("Слово не входит в список");
    }
}
