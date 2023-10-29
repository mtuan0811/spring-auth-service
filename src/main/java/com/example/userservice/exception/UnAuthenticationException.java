package com.example.userservice.exception;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnAuthenticationException extends AuthenticationException {

    public UnAuthenticationException(String message) {
        super(message);
    }
}