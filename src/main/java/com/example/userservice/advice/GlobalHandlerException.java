package com.example.userservice.advice;

import com.example.userservice.domain.dto.ErrorDto;
import com.example.userservice.domain.payload.response.BaseResponse;
import com.example.userservice.exception.ConflictException;
import com.example.userservice.exception.NotFoundException;
import com.example.userservice.exception.TokenRefreshException;
import com.example.userservice.security.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestControllerAdvice
public class GlobalHandlerException {
    private static final Logger logger = LoggerFactory.getLogger(GlobalHandlerException.class);

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<?> validationExceptionHandler(MethodArgumentNotValidException e, WebRequest request) {
        ErrorDto errorDto = new ErrorDto();
        String error = "";
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            error = error.concat("["+ fieldError.getField() +"] " + fieldError.getDefaultMessage() + ",");
        }
//        errorDto.setPath(request.getDescription(false).replace("uri=", ""));
        errorDto.setMessage(error.substring(0, error.length() - 1));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.builder().error(errorDto).build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleUnAuthenticationException(AuthenticationException exception, WebRequest request) {
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage(exception.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseResponse.builder().error(errorDto).build());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException exception, WebRequest request) {
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.builder().error(errorDto).build());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflictException(ConflictException exception, WebRequest request) {
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(BaseResponse.builder().error(errorDto).build());
    }

    @ExceptionHandler({
            TokenRefreshException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<?> handleConflictException(Exception exception, WebRequest request) {
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage(exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BaseResponse.builder().error(errorDto).build());
    }

    @ExceptionHandler({
            Exception.class,
    })
    public ResponseEntity<?> handleAllUncaughtException(Exception exception, WebRequest request) {
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.builder().error(errorDto).build());
    }
}
