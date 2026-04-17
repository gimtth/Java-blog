package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.view.CommentView;
import com.example.blog.model.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CommentMapper extends BaseMapper<Comment> {

    @Select("""
            <script>
            SELECT c.id,
                   c.author_name AS authorName,
                   c.author_email AS authorEmail,
                   c.content,
                   c.status,
                   c.created_at AS createdAt,
                   p.id AS postId,
                   p.title AS postTitle
            FROM comments c
            INNER JOIN posts p ON p.id = c.post_id
            WHERE c.post_id = #{postId}
              AND c.status = #{status}
            ORDER BY c.created_at DESC
            </script>
            """)
    java.util.List<CommentView> selectCommentsByPostIdAndStatus(@Param("postId") Long postId, @Param("status") String status);

    @Select("""
            SELECT c.id,
                   c.author_name AS authorName,
                   c.author_email AS authorEmail,
                   c.content,
                   c.status,
                   c.created_at AS createdAt,
                   p.id AS postId,
                   p.title AS postTitle
            FROM comments c
            INNER JOIN posts p ON p.id = c.post_id
            ORDER BY c.created_at DESC
            """)
    IPage<CommentView> selectCommentPage(Page<CommentView> page);
}
