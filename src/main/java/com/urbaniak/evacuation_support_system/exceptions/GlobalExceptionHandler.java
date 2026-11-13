package com.urbaniak.evacuation_support_system.exceptions;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    /** zgłaszanie ogólne - nieoczekiwany problem */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex){
        System.err.println("wystąpił nieoczekiwany błąd: " + ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Wystąpił nieoczekiwany błąd.");
    }

    /** błąd połączenia z bazą danych */
    @ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    ProblemDetail handleDatabaseConnectionFailure(Exception ex){

        System.err.println("BŁĄD BAZY DANYCH: " + ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Usługa rejestracji niedostepna. Spróbuj ponownie później."
        );
        problemDetail.setTitle("Problem połączenia z bazą danych");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationFailure(MethodArgumentNotValidException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Przesłane dane są nieprawidłowe.");
        problemDetail.setTitle("Błąd walidacji");
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(
                FieldError::getField,
                fe -> fe.getDefaultMessage() == null ? "Nieprawidłowa wartość" : fe.getDefaultMessage(),
                (a, b) -> a
        ));
        problemDetail.setProperty("errors", fieldErrors);
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Nieprawidłowe dane wejściowe");
        return problemDetail;
    }


}
