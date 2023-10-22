package com.example.userservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionUser {
    private String userId;

    private Date createdAt;

    private Boolean isAuthenticate;
}
