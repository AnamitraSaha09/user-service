package com.userservice.web.error;

import com.userservice.service.ProfileAlreadyExistsException;
import com.userservice.service.ProfileNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProfileNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ProfileAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(status, ex.getReason(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
