package ru.practicum.ewm.exceptions;

public class WrongParamUniqueException extends RuntimeException {
    public WrongParamUniqueException(final String message) {
        super(message);
    }
}
