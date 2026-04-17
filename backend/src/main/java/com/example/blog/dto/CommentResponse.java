package com.example.blog.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String authorName,
        String authorEmail,
        String content,
        String status,
        LocalDateTime createdAt,
        Long postId,
        String postTitle
) {
}
