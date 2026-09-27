package com.reset.bus_tracker_interface.api.error;

import com.reset.bus_tracker_interface.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<ApiError>> handleInvalidCredentials(
            InvalidCredentialsException exception,
            ServerWebExchange exchange
    ) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Invalid username or password.",
                exchange
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleNotFound(
            NotFoundException exception,
            ServerWebExchange exchange
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                exception.getMessage(),
                exchange
        );
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<ApiError>> handleConflict(
            ConflictException exception,
            ServerWebExchange exchange
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "CONFLICT",
                exception.getMessage(),
                exchange
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ApiError>> handleBadRequest(
            BadRequestException exception,
            ServerWebExchange exchange
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                exception.getMessage(),
                exchange
        );
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleValidation(
            WebExchangeBindException exception,
            ServerWebExchange exchange
    ) {
        String message = exception.getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request validation failed.");

        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                message,
                exchange
        );
    }

    private Mono<ResponseEntity<ApiError>> buildError(
            HttpStatus status,
            String code,
            String message,
            ServerWebExchange exchange
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity.status(status).body(error));
    }
}