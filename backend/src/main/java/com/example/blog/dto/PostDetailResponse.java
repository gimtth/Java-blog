package com.example.blog.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String content,
        String coverImage,
        CategoryResponse category,
        String status,
        LocalDateTime publishedAt,
        List<CommentResponse> comments
) {
}
