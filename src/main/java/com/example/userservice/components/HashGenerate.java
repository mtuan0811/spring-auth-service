package com.example.userservice.components;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.Date;

@Component
public class HashGenerate {
    public String getHash(String username, Date date) {
        return DigestUtils.md5DigestAsHex(String.format("%s_%d", username, date.getTime()).getBytes());
    }

    public String getKeySessionKey(String username, Date date) {
        return String.format("%s:%s:%s", "session", username, getHash(username, date));
    }
}
