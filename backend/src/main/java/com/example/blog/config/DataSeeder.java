package com.example.blog.config;

import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.PostMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.model.Category;
import com.example.blog.model.Comment;
import com.example.blog.model.CommentStatus;
import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import com.example.blog.model.User;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedBlogData(
            CategoryMapper categoryMapper,
            PostMapper postMapper,
            CommentMapper commentMapper,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.seed-username}") String adminUsername,
            @Value("${app.auth.seed-password}") String adminPassword
    ) {
        return args -> {
            if (userMapper.selectCount(null) == 0) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setDisplayName("博客管理员");
                admin.setRole("ADMIN");
                admin.setCreatedAt(LocalDateTime.now());
                userMapper.insert(admin);
            }

            if (categoryMapper.selectCount(null) > 0) {
                return;
            }

            Category backend = new Category();
            backend.setName("Java 后端");
            backend.setSlug("java-backend");

            Category interview = new Category();
            interview.setName("面试准备");
            interview.setSlug("interview");

            categoryMapper.insert(backend);
            categoryMapper.insert(interview);

            Post firstPost = new Post();
            firstPost.setTitle("Spring Boot 博客系统搭建记录");
            firstPost.setSlug("spring-boot-blog-system");
            firstPost.setExcerpt("从接口设计、分类管理到评论审核，一次梳理博客管理系统的核心模块。");
            firstPost.setContent("这个演示项目包含公开博客页和后台管理台。后端使用 Spring Boot + MyBatis-Plus + Redis，前端使用 Vue 3。你可以在此基础上继续扩展标签、文件上传和更多运营能力。");
            firstPost.setCategoryId(backend.getId());
            firstPost.setStatus(PostStatus.PUBLISHED);
            firstPost.setCreatedAt(LocalDateTime.now().minusDays(7));
            firstPost.setUpdatedAt(LocalDateTime.now().minusDays(1));
            firstPost.setPublishedAt(LocalDateTime.now().minusDays(6));

            Post secondPost = new Post();
            secondPost.setTitle("Java 后端开发学习清单");
            secondPost.setSlug("java-backend-learning-checklist");
            secondPost.setExcerpt("整理 Java 后端开发常见知识点，包括集合、多线程、Spring Boot、MySQL 和 Redis。");
            secondPost.setContent("写博客也是整理知识体系的一种方式。你可以把学习过程沉淀成文章，再通过这个项目统一维护和展示。");
            secondPost.setCategoryId(interview.getId());
            secondPost.setStatus(PostStatus.PUBLISHED);
            secondPost.setCreatedAt(LocalDateTime.now().minusDays(5));
            secondPost.setUpdatedAt(LocalDateTime.now().minusDays(2));
            secondPost.setPublishedAt(LocalDateTime.now().minusDays(5));

            postMapper.insert(firstPost);
            postMapper.insert(secondPost);

            Comment approved = new Comment();
            approved.setPostId(firstPost.getId());
            approved.setAuthorName("Codex");
            approved.setAuthorEmail("codex@example.com");
            approved.setContent("这篇文章结构清晰，适合放进你的项目经历。");
            approved.setStatus(CommentStatus.APPROVED);
            approved.setCreatedAt(LocalDateTime.now().minusDays(3));

            Comment pending = new Comment();
            pending.setPostId(secondPost.getId());
            pending.setAuthorName("HR Reader");
            pending.setAuthorEmail("hr@example.com");
            pending.setContent("如果补一段性能优化或缓存实践，会更像真实项目。");
            pending.setStatus(CommentStatus.PENDING);
            pending.setCreatedAt(LocalDateTime.now().minusHours(12));

            commentMapper.insert(approved);
            commentMapper.insert(pending);
        };
    }
}
