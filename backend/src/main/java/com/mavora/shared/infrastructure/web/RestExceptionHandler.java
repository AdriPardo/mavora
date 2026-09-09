package com.mavora.shared.infrastructure.web;

import com.mavora.shared.domain.DomainException;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleDomain(DomainException exception) {
        HttpStatus status = switch (exception.type()) {
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case RULE -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        if (exception.type() == DomainException.ErrorType.UNAUTHENTICATED) {
            log.info("Authentication failed");
        } else {
            log.info("Domain exception {}: {}", exception.type(), exception.getMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(exception.title());
        problem.setDetail(exception.getMessage());
        problem.setType(URI.create("https://mavora.invalid/problems/" + exception.type().name().toLowerCase()));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setDetail("The request body is invalid");
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(RestExceptionHandler::formatFieldError)
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid request");
        problem.setDetail(exception.getMessage());
        return problem;
    }

    private static String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
