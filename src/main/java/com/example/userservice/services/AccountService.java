package com.example.userservice.services;

import com.example.userservice.models.RefreshToken;

public interface AccountService {
    public RefreshToken createRefreshToken(String userId);
}
