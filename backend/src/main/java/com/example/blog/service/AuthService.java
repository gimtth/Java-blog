package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.LoginResponse;
import com.example.blog.mapper.UserMapper;
import com.example.blog.model.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final long expirationMinutes;
    private final SecretKey secretKey;

    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.jwt-secret}") String jwtSecret,
            @Value("${app.auth.jwt-expiration-minutes}") long expirationMinutes
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.expirationMinutes = expirationMinutes;
        this.secretKey = buildSecretKey(jwtSecret);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()).last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(user.getUsername())
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
