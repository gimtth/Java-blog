package com.example.blog.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInMinutes
) {
}
