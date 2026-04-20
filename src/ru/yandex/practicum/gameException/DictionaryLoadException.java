package ru.yandex.practicum.gameException;

//Исключение список пуст
public class DictionaryLoadException extends RuntimeException {
    public DictionaryLoadException(Throwable cause) {
        super("Список не загружен", cause);
    }
}
