package com.example.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank String authorName,
        @Email @NotBlank String authorEmail,
        @NotBlank String content
) {
}
