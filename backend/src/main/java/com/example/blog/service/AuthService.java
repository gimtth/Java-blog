package com.example.blog.service;

import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.LoginResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final String adminUsername;
    private final String adminPassword;
    private final long expirationMinutes;
    private final SecretKey secretKey;

    public AuthService(
            @Value("${app.auth.username}") String adminUsername,
            @Value("${app.auth.password}") String adminPassword,
            @Value("${app.auth.jwt-secret}") String jwtSecret,
            @Value("${app.auth.jwt-expiration-minutes}") long expirationMinutes
    ) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.expirationMinutes = expirationMinutes;
        this.secretKey = buildSecretKey(jwtSecret);
    }

    public LoginResponse login(LoginRequest request) {
        if (!adminUsername.equals(request.username()) || !adminPassword.equals(request.password())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(request.username())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();

        return new LoginResponse(token, "Bearer", expirationMinutes);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey buildSecretKey(String jwtSecret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException exception) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
