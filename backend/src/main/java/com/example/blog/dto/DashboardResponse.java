package com.example.blog.dto;

public record DashboardResponse(
        long totalPosts,
        long publishedPosts,
        long totalCategories,
        long pendingComments
) {
}
