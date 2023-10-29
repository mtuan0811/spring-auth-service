package com.example.userservice.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class JwtDto {
    private String token;
    private String type = "Bearer";
    private String refreshToken;
    private String id;
    private String username;
    private String email;
    private List<String> roles;

    public JwtDto(String accessToken, String refreshToken, String id, String username, String email, List<String> roles) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
