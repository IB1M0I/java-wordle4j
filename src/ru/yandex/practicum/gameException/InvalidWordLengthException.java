package ru.yandex.practicum.gameException;


//Ответ пользователя больше 5 букв
public class InvalidWordLengthException extends RuntimeException {
    public InvalidWordLengthException() {
        super("ОШИБКА: ответ длинее/меньше 5 символов");
    }
}
