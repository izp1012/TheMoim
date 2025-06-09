package com.moim.payment.config.auth;

import org.springframework.stereotype.Component;

@Component
public class JwtVO {

    public static final String SECRET = "your-secure-secret-key-that-is-at-least-256-bits-long";
    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;  // 만료시간 1주일
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}