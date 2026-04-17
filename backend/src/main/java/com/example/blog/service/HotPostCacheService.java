package com.example.blog.service;

import com.example.blog.dto.PostDetailResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class HotPostCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public HotPostCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.cache.hot-post-ttl-minutes}") long ttlMinutes
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public Optional<PostDetailResponse> getPublishedPost(String slug) {
        try {
            String value = redisTemplate.opsForValue().get(buildKey(slug));
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(value, PostDetailResponse.class));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public void cachePublishedPost(String slug, PostDetailResponse response) {
        try {
            redisTemplate.opsForValue().set(buildKey(slug), writeJson(response), ttl);
        } catch (Exception exception) {
            // Redis is optional in local development. Ignore cache failures and fall back to DB.
        }
    }

    public void evictPublishedPost(String slug) {
        try {
            redisTemplate.delete(buildKey(slug));
        } catch (Exception exception) {
            // Ignore cache eviction failure to keep write path available.
        }
    }

    private String writeJson(PostDetailResponse response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(response);
    }

    private String buildKey(String slug) {
        return "blog:post:hot:" + slug;
    }
}
