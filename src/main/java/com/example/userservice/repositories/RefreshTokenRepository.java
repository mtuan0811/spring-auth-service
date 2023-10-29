package com.example.userservice.repositories;

import com.example.userservice.domain.models.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository{
    Optional<RefreshToken> findByRefeshToken(String token);
    void createRefeshToken(RefreshToken refreshToken);
    long deleteToken(RefreshToken refreshToken);
    long deleteTokenByUser(String userId);
}