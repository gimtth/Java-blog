package com.example.blog.repository;

import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByStatusOrderByPublishedAtDesc(PostStatus status);
    List<Post> findByStatusAndCategory_SlugOrderByPublishedAtDesc(PostStatus status, String categorySlug);
    Optional<Post> findBySlug(String slug);
}
