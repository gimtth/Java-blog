package com.example.blog.config;

import com.example.blog.model.Category;
import com.example.blog.model.Comment;
import com.example.blog.model.CommentStatus;
import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedBlogData(CategoryRepository categoryRepository, PostRepository postRepository, CommentRepository commentRepository) {
        return args -> {
            if (categoryRepository.count() > 0) {
                return;
            }

            Category backend = new Category();
            backend.setName("Java 后端");
            backend.setSlug("java-backend");

            Category interview = new Category();
            interview.setName("面试准备");
            interview.setSlug("interview");

            categoryRepository.save(backend);
            categoryRepository.save(interview);

            Post firstPost = new Post();
            firstPost.setTitle("Spring Boot 博客系统搭建记录");
            firstPost.setSlug("spring-boot-blog-system");
            firstPost.setExcerpt("从接口设计、分类管理到评论审核，一次梳理博客管理系统的核心模块。");
            firstPost.setContent("这个演示项目包含公开博客页和后台管理台。后端使用 Spring Boot + Spring Security + JPA，前端使用 Vue 3。你可以在此基础上继续扩展标签、文件上传和分页。");
            firstPost.setCategory(backend);
            firstPost.setStatus(PostStatus.PUBLISHED);
            firstPost.setCreatedAt(LocalDateTime.now().minusDays(7));
            firstPost.setUpdatedAt(LocalDateTime.now().minusDays(1));
            firstPost.setPublishedAt(LocalDateTime.now().minusDays(6));

            Post secondPost = new Post();
            secondPost.setTitle("Java 后端开发学习清单");
            secondPost.setSlug("java-backend-learning-checklist");
            secondPost.setExcerpt("整理 Java 后端开发常见知识点，包括集合、多线程、Spring Boot、MySQL 和 Redis。");
            secondPost.setContent("写博客也是整理知识体系的一种方式。你可以把学习过程沉淀成文章，再通过这个项目统一维护和展示。");
            secondPost.setCategory(interview);
            secondPost.setStatus(PostStatus.PUBLISHED);
            secondPost.setCreatedAt(LocalDateTime.now().minusDays(5));
            secondPost.setUpdatedAt(LocalDateTime.now().minusDays(2));
            secondPost.setPublishedAt(LocalDateTime.now().minusDays(5));

            postRepository.save(firstPost);
            postRepository.save(secondPost);

            Comment approved = new Comment();
            approved.setPost(firstPost);
            approved.setAuthorName("Codex");
            approved.setAuthorEmail("codex@example.com");
            approved.setContent("这篇文章结构清晰，适合放进你的项目经历。");
            approved.setStatus(CommentStatus.APPROVED);
            approved.setCreatedAt(LocalDateTime.now().minusDays(3));

            Comment pending = new Comment();
            pending.setPost(secondPost);
            pending.setAuthorName("HR Reader");
            pending.setAuthorEmail("hr@example.com");
            pending.setContent("如果补一段性能优化或缓存实践，会更像真实项目。");
            pending.setStatus(CommentStatus.PENDING);
            pending.setCreatedAt(LocalDateTime.now().minusHours(12));

            commentRepository.save(approved);
            commentRepository.save(pending);
        };
    }
}
