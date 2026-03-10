package com.project.recon.global.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

    public final static String ACCESS_TOKEN_COOKIE = "access_token";
    public final static String REFRESH_TOKEN_COOKIE = "refresh_token";

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    @Value("${jwt.cookie-secure:false}")
    private boolean secure;

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(accessTokenExpiration / 1000)
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(refreshTokenExpiration / 1000)
                .build();
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }
}
