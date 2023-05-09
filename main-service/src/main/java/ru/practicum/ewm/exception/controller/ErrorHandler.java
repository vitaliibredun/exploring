package ru.practicum.ewm.exception.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.IncorrectInputException;
import ru.practicum.ewm.exception.NotExistsException;
import ru.practicum.ewm.exception.model.ApiError;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception exception) {
        log.warn("500 {}", exception.getMessage(), exception);
        ApiError error = new ApiError();
        error.setMessage(exception.getMessage());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
        error.setTimestamp(LocalDateTime.now());
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotExistsException(final NotExistsException exception) {
        log.warn("404 {}", exception.getMessage(), exception);
        ApiError error = new ApiError();
        error.setMessage(exception.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND.name());
        error.setTimestamp(LocalDateTime.now());
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleNotExistsException(final IncorrectInputException exception) {
        log.warn("403 {}", exception.getMessage(), exception);
        ApiError error = new ApiError();
        error.setMessage(exception.getMessage());
        error.setStatus(HttpStatus.FORBIDDEN.name());
        error.setTimestamp(LocalDateTime.now());
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException exception) {
        log.warn("409 {}", exception.getMessage(), exception);
        ApiError error = new ApiError();
        error.setMessage(exception.getMessage());
        error.setStatus(HttpStatus.CONFLICT.name());
        error.setTimestamp(LocalDateTime.now());
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final BadRequestException exception) {
        log.warn("400 {}", exception.getMessage(), exception);
        ApiError error = new ApiError();
        error.setMessage(exception.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.name());
        error.setTimestamp(LocalDateTime.now());
        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
