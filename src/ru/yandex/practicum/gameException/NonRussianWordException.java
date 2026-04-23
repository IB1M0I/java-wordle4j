package ru.yandex.practicum.gameException;


//Исключение ответ не на руссеом языке
public class NonRussianWordException extends RuntimeException {
    public NonRussianWordException() {
        super("ОШИБКА: ответ содержит нерусские символы");
    }
}
