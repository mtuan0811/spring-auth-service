package com.example.userservice.advice;

import com.example.userservice.exception.ConflictException;
import com.example.userservice.exception.TokenRefreshException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.payload.response.ErrorResponse;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalHandlerException {
    private static final Logger logger = LoggerFactory.getLogger(GlobalHandlerException.class);

    @ExceptionHandler({
            ValidationException.class,
            TokenRefreshException.class,
            ConflictException.class,
            UserNotFoundException.class,
            Exception.class,
    })
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception exception, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.addError(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
