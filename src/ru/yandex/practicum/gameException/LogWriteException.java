package ru.yandex.practicum.gameException;


//Исключение записи в лог
public class LogWriteException extends Exception {
    public LogWriteException(Throwable cause) {
        super("ОШИБКА: записи процесса игры", cause);
    }
}
