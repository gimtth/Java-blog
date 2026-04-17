package com.example.blog.controller;

import com.example.blog.dto.CategoryRequest;
import com.example.blog.dto.CategoryResponse;
import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.DashboardResponse;
import com.example.blog.dto.PostRequest;
import com.example.blog.dto.PostDetailResponse;
import com.example.blog.dto.PostSummaryResponse;
import com.example.blog.service.BlogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminBlogController {

    private final BlogService blogService;

    public AdminBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return blogService.getDashboard();
    }

    @GetMapping("/posts")
    public List<PostSummaryResponse> getPosts() {
        return blogService.getAllPosts();
    }

    @GetMapping("/posts/{id}")
    public PostDetailResponse getPost(@PathVariable Long id) {
        return blogService.getAdminPost(id);
    }

    @PostMapping("/posts")
    public PostSummaryResponse createPost(@Valid @RequestBody PostRequest request) {
        return blogService.savePost(request, null);
    }

    @PutMapping("/posts/{id}")
    public PostSummaryResponse updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        return blogService.savePost(request, id);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable Long id) {
        blogService.deletePost(id);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getCategories() {
        return blogService.getCategories();
    }

    @PostMapping("/categories")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return blogService.saveCategory(request, null);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return blogService.saveCategory(request, id);
    }

    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@PathVariable Long id) {
        blogService.deleteCategory(id);
    }

    @GetMapping("/comments")
    public List<CommentResponse> getComments() {
        return blogService.getAllComments();
    }

    @PatchMapping("/comments/{id}/approve")
    public CommentResponse approveComment(@PathVariable Long id) {
        return blogService.approveComment(id);
    }

    @DeleteMapping("/comments/{id}")
    public void deleteComment(@PathVariable Long id) {
        blogService.deleteComment(id);
    }
}
