package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.view.PostDetailView;
import com.example.blog.dto.view.PostSummaryView;
import com.example.blog.model.Post;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PostMapper extends BaseMapper<Post> {

    @Select("""
            <script>
            SELECT p.id,
                   p.title,
                   p.slug,
                   p.excerpt,
                   c.name AS categoryName,
                   c.slug AS categorySlug,
                   p.status,
                   p.published_at AS publishedAt
            FROM posts p
            INNER JOIN categories c ON c.id = p.category_id
            WHERE p.status = #{status}
            <if test="categorySlug != null and categorySlug != ''">
              AND c.slug = #{categorySlug}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (LOWER(p.title) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                   OR LOWER(p.excerpt) LIKE CONCAT('%', LOWER(#{keyword}), '%'))
            </if>
            ORDER BY p.published_at DESC, p.id DESC
            </script>
            """)
    IPage<PostSummaryView> selectPublishedPostPage(
            Page<PostSummaryView> page,
            @Param("status") String status,
            @Param("categorySlug") String categorySlug,
            @Param("keyword") String keyword
    );

    @Select("""
            SELECT p.id,
                   p.title,
                   p.slug,
                   p.excerpt,
                   c.name AS categoryName,
                   c.slug AS categorySlug,
                   p.status,
                   p.published_at AS publishedAt
            FROM posts p
            INNER JOIN categories c ON c.id = p.category_id
            ORDER BY p.updated_at DESC, p.id DESC
            """)
    IPage<PostSummaryView> selectAdminPostPage(Page<PostSummaryView> page);

    @Select("""
            SELECT p.id,
                   p.title,
                   p.slug,
                   p.excerpt,
                   p.content,
                   p.cover_image AS coverImage,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   c.slug AS categorySlug,
                   p.status,
                   p.published_at AS publishedAt
            FROM posts p
            INNER JOIN categories c ON c.id = p.category_id
            WHERE p.slug = #{slug}
              AND p.status = #{status}
            LIMIT 1
            """)
    PostDetailView selectPublishedPostDetailBySlug(@Param("slug") String slug, @Param("status") String status);

    @Select("""
            SELECT p.id,
                   p.title,
                   p.slug,
                   p.excerpt,
                   p.content,
                   p.cover_image AS coverImage,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   c.slug AS categorySlug,
                   p.status,
                   p.published_at AS publishedAt
            FROM posts p
            INNER JOIN categories c ON c.id = p.category_id
            WHERE p.id = #{id}
            LIMIT 1
            """)
    PostDetailView selectPostDetailById(@Param("id") Long id);
}
