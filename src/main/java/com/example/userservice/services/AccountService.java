package com.example.userservice.services;

import com.example.userservice.domain.dto.JwtDto;
import com.example.userservice.domain.dto.TokenRefreshDto;
import com.example.userservice.domain.payload.request.LoginRequest;
import com.example.userservice.domain.payload.request.SignupRequest;
import com.example.userservice.domain.payload.request.TokenRefreshRequest;

public interface AccountService {
    public TokenRefreshDto refreshToken(TokenRefreshRequest request);
    public JwtDto signUser(LoginRequest loginRequest);
    public void registerUser(SignupRequest signupRequest);
    public void logoutUser();
}
