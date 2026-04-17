package com.example.blog.controller;

import com.example.blog.dto.CategoryResponse;
import com.example.blog.dto.CommentRequest;
import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.PostDetailResponse;
import com.example.blog.dto.PostSummaryResponse;
import com.example.blog.service.BlogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicBlogController {

    private final BlogService blogService;

    public PublicBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/posts")
    public List<PostSummaryResponse> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        return blogService.getPublishedPosts(category, keyword);
    }

    @GetMapping("/posts/{slug}")
    public PostDetailResponse getPost(@PathVariable String slug) {
        return blogService.getPublishedPost(slug);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getCategories() {
        return blogService.getCategories();
    }

    @PostMapping("/posts/{postId}/comments")
    public CommentResponse submitComment(@PathVariable Long postId, @Valid @RequestBody CommentRequest request) {
        return blogService.submitComment(postId, request);
    }
}
