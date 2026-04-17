package com.example.blog.service;

import com.example.blog.dto.CategoryRequest;
import com.example.blog.dto.CategoryResponse;
import com.example.blog.dto.CommentRequest;
import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.DashboardResponse;
import com.example.blog.dto.PostDetailResponse;
import com.example.blog.dto.PostRequest;
import com.example.blog.dto.PostSummaryResponse;
import com.example.blog.model.Category;
import com.example.blog.model.Comment;
import com.example.blog.model.CommentStatus;
import com.example.blog.model.Post;
import com.example.blog.model.PostStatus;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlogService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;

    public BlogService(PostRepository postRepository, CategoryRepository categoryRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getPublishedPosts(String categorySlug, String keyword) {
        List<Post> posts = (categorySlug == null || categorySlug.isBlank())
                ? postRepository.findByStatusOrderByPublishedAtDesc(PostStatus.PUBLISHED)
                : postRepository.findByStatusAndCategory_SlugOrderByPublishedAtDesc(PostStatus.PUBLISHED, categorySlug);

        return posts.stream()
                .filter(post -> keyword == null || keyword.isBlank() || containsIgnoreCase(post.getTitle(), keyword) || containsIgnoreCase(post.getExcerpt(), keyword))
                .map(this::toPostSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPublishedPost(String slug) {
        Post post = postRepository.findBySlug(slug)
                .filter(item -> item.getStatus() == PostStatus.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        List<CommentResponse> comments = commentRepository.findByPost_IdAndStatusOrderByCreatedAtDesc(post.getId(), CommentStatus.APPROVED)
                .stream()
                .map(this::toCommentResponse)
                .toList();
        return toPostDetail(post, comments);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream().map(this::toCategoryResponse).toList();
    }

    public CommentResponse submitComment(Long postId, CommentRequest request) {
        Post post = getPost(postId);
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthorName(request.authorName());
        comment.setAuthorEmail(request.authorEmail());
        comment.setContent(request.content());
        comment.setStatus(CommentStatus.PENDING);
        comment.setCreatedAt(LocalDateTime.now());
        return toCommentResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalPosts = postRepository.count();
        long publishedPosts = postRepository.findByStatusOrderByPublishedAtDesc(PostStatus.PUBLISHED).size();
        long totalCategories = categoryRepository.count();
        long pendingComments = commentRepository.countByStatus(CommentStatus.PENDING);
        return new DashboardResponse(totalPosts, publishedPosts, totalCategories, pendingComments);
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getAllPosts() {
        return postRepository.findAll().stream().map(this::toPostSummary).toList();
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getAdminPost(Long id) {
        Post post = getPost(id);
        List<CommentResponse> comments = commentRepository.findByPost_IdAndStatusOrderByCreatedAtDesc(post.getId(), CommentStatus.APPROVED)
                .stream()
                .map(this::toCommentResponse)
                .toList();
        return toPostDetail(post, comments);
    }

    public PostSummaryResponse savePost(PostRequest request, Long postId) {
        Post post = postId == null ? new Post() : getPost(postId);
        post.setTitle(request.title());
        post.setSlug(request.slug());
        post.setExcerpt(request.excerpt());
        post.setContent(request.content());
        post.setCoverImage(request.coverImage());
        post.setStatus(request.status());
        post.setCategory(getCategory(request.categoryId()));
        LocalDateTime now = LocalDateTime.now();
        if (postId == null) {
            post.setCreatedAt(now);
        }
        post.setUpdatedAt(now);
        post.setPublishedAt(request.status() == PostStatus.PUBLISHED ? now : null);
        return toPostSummary(postRepository.save(post));
    }

    public void deletePost(Long id) {
        postRepository.delete(getPost(id));
    }

    public CategoryResponse saveCategory(CategoryRequest request, Long categoryId) {
        Category category = categoryId == null ? new Category() : getCategory(categoryId);
        category.setName(request.name());
        category.setSlug(request.slug());
        return toCategoryResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        categoryRepository.delete(getCategory(id));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getAllComments() {
        return commentRepository.findAll().stream().map(this::toCommentResponse).toList();
    }

    public CommentResponse approveComment(Long id) {
        Comment comment = getComment(id);
        comment.setStatus(CommentStatus.APPROVED);
        return toCommentResponse(commentRepository.save(comment));
    }

    public void deleteComment(Long id) {
        commentRepository.delete(getComment(id));
    }

    private Post getPost(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    private Comment getComment(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Comment not found"));
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug());
    }

    private PostSummaryResponse toPostSummary(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getCategory().getName(),
                post.getCategory().getSlug(),
                post.getStatus().name(),
                post.getPublishedAt()
        );
    }

    private PostDetailResponse toPostDetail(Post post, List<CommentResponse> comments) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                post.getCoverImage(),
                toCategoryResponse(post.getCategory()),
                post.getStatus().name(),
                post.getPublishedAt(),
                comments
        );
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthorName(),
                comment.getAuthorEmail(),
                comment.getContent(),
                comment.getStatus().name(),
                comment.getCreatedAt(),
                comment.getPost().getId(),
                comment.getPost().getTitle()
        );
    }
}
