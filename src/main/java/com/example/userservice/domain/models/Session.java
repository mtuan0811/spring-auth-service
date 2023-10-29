package com.example.userservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "Session", timeToLive = 120L)
public class Session {
    @Id
    @Indexed
    private String userId;

    @Indexed
    private String refreshToken;

    @TimeToLive
    private Long remainingTime;

}
