package com.vanguard.weatherapp.fixture;

import com.vanguard.weatherapp.entity.UserToken;

import java.time.OffsetDateTime;

public class StubsUserToken {

    public static UserToken stubUserToken(String token) {
        return UserToken.builder()
                .id(1L)
                .token(token)
                .rateLimit(5)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
