package ru.practicum.ewm.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(final String message) {
        super(message);
    }
}

