package com.example.blog.dto;

import com.example.blog.model.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostRequest(
        @NotBlank String title,
        @NotBlank String slug,
        @NotBlank String excerpt,
        @NotBlank String content,
        String coverImage,
        @NotNull Long categoryId,
        @NotNull PostStatus status
) {
}
