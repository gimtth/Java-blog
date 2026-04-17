package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.CategoryRequest;
import com.example.blog.dto.CategoryResponse;
import com.example.blog.dto.CommentRequest;
import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.DashboardResponse;
import com.example.blog.dto.PageResponse;
import com.example.blog.dto.PostDetailResponse;
import com.example.blog.dto.PostRequest;
import com.example.blog.dto.PostSummaryResponse;
import com.example.blog.dto.view.CommentView;
import com.example.blog.dto.view.PostDetailView;
import com.example.blog.dto.view.PostSummaryView;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.PostMapper;
import com.example.blog.model.Category;
import com.example.blog.model.Comment;
import com.example.blog.model.CommentStatus;
import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlogService {

    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final CommentMapper commentMapper;
    private final HotPostCacheService hotPostCacheService;

    public BlogService(
            PostMapper postMapper,
            CategoryMapper categoryMapper,
            CommentMapper commentMapper,
            HotPostCacheService hotPostCacheService
    ) {
        this.postMapper = postMapper;
        this.categoryMapper = categoryMapper;
        this.commentMapper = commentMapper;
        this.hotPostCacheService = hotPostCacheService;
    }

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getPublishedPosts(String categorySlug, String keyword, long current, long size) {
        IPage<PostSummaryView> page = postMapper.selectPublishedPostPage(
                new Page<>(current, size),
                PostStatus.PUBLISHED.name(),
                categorySlug,
                keyword
        );
        return toPageResponse(page.getRecords().stream().map(this::toPostSummary).toList(), page);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPublishedPost(String slug) {
        return hotPostCacheService.getPublishedPost(slug).orElseGet(() -> {
            PostDetailView post = postMapper.selectPublishedPostDetailBySlug(slug, PostStatus.PUBLISHED.name());
            if (post == null) {
                throw new ResourceNotFoundException("Post not found");
            }
            PostDetailResponse response = toPostDetail(post, getApprovedComments(post.getId()));
            hotPostCacheService.cachePublishedPost(slug, response);
            return response;
        });
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getName))
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public CommentResponse submitComment(Long postId, CommentRequest request) {
        Post post = getPost(postId);
        Comment comment = new Comment();
        comment.setPostId(post.getId());
        comment.setAuthorName(request.authorName());
        comment.setAuthorEmail(request.authorEmail());
        comment.setContent(request.content());
        comment.setStatus(CommentStatus.PENDING);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        return toCommentResponse(comment, post.getTitle());
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalPosts = postMapper.selectCount(null);
        long publishedPosts = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, PostStatus.PUBLISHED));
        long totalCategories = categoryMapper.selectCount(null);
        long pendingComments = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, CommentStatus.PENDING));
        return new DashboardResponse(totalPosts, publishedPosts, totalCategories, pendingComments);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getAllPosts(long current, long size) {
        IPage<PostSummaryView> page = postMapper.selectAdminPostPage(new Page<>(current, size));
        return toPageResponse(page.getRecords().stream().map(this::toPostSummary).toList(), page);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getAdminPost(Long id) {
        PostDetailView post = postMapper.selectPostDetailById(id);
        if (post == null) {
            throw new ResourceNotFoundException("Post not found");
        }
        return toPostDetail(post, getApprovedComments(post.getId()));
    }

    public PostSummaryResponse savePost(PostRequest request, Long postId) {
        Category category = getCategory(request.categoryId());
        Post post = postId == null ? new Post() : getPost(postId);
        String originalSlug = post.getSlug();

        post.setTitle(request.title());
        post.setSlug(request.slug());
        post.setExcerpt(request.excerpt());
        post.setContent(request.content());
        post.setCoverImage(request.coverImage());
        post.setStatus(request.status());
        post.setCategoryId(category.getId());

        LocalDateTime now = LocalDateTime.now();
        if (postId == null) {
            post.setCreatedAt(now);
        }
        post.setUpdatedAt(now);
        post.setPublishedAt(request.status() == PostStatus.PUBLISHED ? now : null);

        if (postId == null) {
            postMapper.insert(post);
        } else {
            postMapper.updateById(post);
            if (originalSlug != null && !originalSlug.equals(post.getSlug())) {
                hotPostCacheService.evictPublishedPost(originalSlug);
            }
        }

        hotPostCacheService.evictPublishedPost(post.getSlug());
        return toPostSummary(post, category);
    }

    public void deletePost(Long id) {
        Post post = getPost(id);
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, id));
        postMapper.deleteById(id);
        hotPostCacheService.evictPublishedPost(post.getSlug());
    }

    public CategoryResponse saveCategory(CategoryRequest request, Long categoryId) {
        Category category = categoryId == null ? new Category() : getCategory(categoryId);
        category.setName(request.name());
        category.setSlug(request.slug());
        if (categoryId == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
        return toCategoryResponse(category);
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(getCategory(id).getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getAllComments(long current, long size) {
        IPage<CommentView> page = commentMapper.selectCommentPage(new Page<>(current, size));
        return toPageResponse(page.getRecords().stream().map(this::toCommentResponse).toList(), page);
    }

    public CommentResponse approveComment(Long id) {
        Comment comment = getComment(id);
        comment.setStatus(CommentStatus.APPROVED);
        commentMapper.updateById(comment);
        Post post = getPost(comment.getPostId());
        hotPostCacheService.evictPublishedPost(post.getSlug());
        return toCommentResponse(comment, post.getTitle());
    }

    public void deleteComment(Long id) {
        Comment comment = getComment(id);
        commentMapper.deleteById(id);
        Post post = getPost(comment.getPostId());
        hotPostCacheService.evictPublishedPost(post.getSlug());
    }

    private List<CommentResponse> getApprovedComments(Long postId) {
        return commentMapper.selectCommentsByPostIdAndStatus(postId, CommentStatus.APPROVED.name())
                .stream()
                .map(this::toCommentResponse)
                .toList();
    }

    private Post getPost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new ResourceNotFoundException("Post not found");
        }
        return post;
    }

    private Category getCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private Comment getComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment not found");
        }
        return comment;
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug());
    }

    private PostSummaryResponse toPostSummary(PostSummaryView post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getCategoryName(),
                post.getCategorySlug(),
                post.getStatus(),
                post.getPublishedAt()
        );
    }

    private PostSummaryResponse toPostSummary(Post post, Category category) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                category.getName(),
                category.getSlug(),
                post.getStatus().name(),
                post.getPublishedAt()
        );
    }

    private PostDetailResponse toPostDetail(PostDetailView post, List<CommentResponse> comments) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                post.getCoverImage(),
                new CategoryResponse(post.getCategoryId(), post.getCategoryName(), post.getCategorySlug()),
                post.getStatus(),
                post.getPublishedAt(),
                comments
        );
    }

    private CommentResponse toCommentResponse(CommentView comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthorName(),
                comment.getAuthorEmail(),
                comment.getContent(),
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getPostId(),
                comment.getPostTitle()
        );
    }

    private CommentResponse toCommentResponse(Comment comment, String postTitle) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthorName(),
                comment.getAuthorEmail(),
                comment.getContent(),
                comment.getStatus().name(),
                comment.getCreatedAt(),
                comment.getPostId(),
                postTitle
        );
    }

    private <T> PageResponse<T> toPageResponse(List<T> records, IPage<?> page) {
        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }
}
