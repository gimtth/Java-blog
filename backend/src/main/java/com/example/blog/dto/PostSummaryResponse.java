package com.example.blog.dto;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String categoryName,
        String categorySlug,
        String status,
        LocalDateTime publishedAt
) {
}
