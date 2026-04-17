CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    slug VARCHAR(180) NOT NULL UNIQUE,
    excerpt VARCHAR(300) NOT NULL,
    content TEXT NOT NULL,
    cover_image VARCHAR(255),
    status VARCHAR(16) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP NULL,
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_name VARCHAR(80) NOT NULL,
    author_email VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id)
);

CREATE INDEX IF NOT EXISTS idx_posts_status_published_at ON posts(status, published_at);
CREATE INDEX IF NOT EXISTS idx_posts_category_id ON posts(category_id);
CREATE INDEX IF NOT EXISTS idx_comments_post_status ON comments(post_id, status);
