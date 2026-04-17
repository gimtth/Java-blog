package com.example.blog.repository;

import com.example.blog.model.Comment;
import com.example.blog.model.CommentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_IdAndStatusOrderByCreatedAtDesc(Long postId, CommentStatus status);
    long countByStatus(CommentStatus status);
}
