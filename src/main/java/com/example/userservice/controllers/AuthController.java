package com.example.userservice.controllers;

import com.example.userservice.domain.dto.ErrorDto;
import com.example.userservice.domain.dto.JwtDto;
import com.example.userservice.domain.dto.MessageDto;
import com.example.userservice.domain.dto.TokenRefreshDto;
import com.example.userservice.domain.payload.request.LoginRequest;
import com.example.userservice.domain.payload.request.SignupRequest;
import com.example.userservice.domain.payload.request.TokenRefreshRequest;
import com.example.userservice.domain.payload.response.BaseResponse;
import com.example.userservice.security.jwt.JwtUtils;
import com.example.userservice.services.impl.AccountServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CrossOrigin;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AccountServiceImpl accountService;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtDto jwtDto = accountService.signUser(loginRequest);
        ResponseCookie cookieToken = jwtUtils.generateJwtCookie(jwtDto.getToken());
        ResponseCookie cookieRefresh = jwtUtils.generateRefreshTokenCookie(jwtDto.getRefreshToken());
        BaseResponse<Object> jwtResponse = BaseResponse.builder().data(jwtDto).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieToken.toString())
                .header(HttpHeaders.SET_COOKIE, cookieRefresh.toString())
                .body(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        accountService.registerUser(signUpRequest);
        return ResponseEntity.ok(BaseResponse.builder().error(new ErrorDto("User registered successfully!")).build());
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        accountService.logoutUser();
        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = jwtUtils.getCleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(BaseResponse.builder().data(new MessageDto("You've been signed out!")).build());
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@RequestBody TokenRefreshRequest request) {
        TokenRefreshDto tokenRefreshDto = accountService.refreshToken(request);
        ResponseCookie cookieToken = jwtUtils.generateJwtCookie(tokenRefreshDto.getAccessToken());
        ResponseCookie cookieRefresh = jwtUtils.generateRefreshTokenCookie(tokenRefreshDto.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieToken.toString())
                .header(HttpHeaders.SET_COOKIE, cookieRefresh.toString())
                .body(BaseResponse.builder().data(tokenRefreshDto).build());
    }
}