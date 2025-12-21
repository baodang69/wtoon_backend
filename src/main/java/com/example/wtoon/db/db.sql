-- ==========================================
-- 1. NHÓM DỮ LIỆU CỐT LÕI (CORE DATA)
-- ==========================================

-- Bảng Category (Thể loại)
CREATE TABLE category (
                          id VARCHAR(255) PRIMARY KEY,
                          name VARCHAR(255),
                          slug VARCHAR(255) UNIQUE NOT NULL
);
CREATE INDEX idx_category_slug ON category (slug);

-- Bảng Story (Truyện)
CREATE TABLE story (
                       id VARCHAR(255) PRIMARY KEY,
                       slug VARCHAR(255) UNIQUE NOT NULL,
                       name VARCHAR(255),
                       status VARCHAR(255),
                       thumb_url VARCHAR(255),
                       cdn_domain VARCHAR(255),
                       description TEXT,
                       author VARCHAR(255),
                       view_count BIGINT DEFAULT 0,
                       created_at TIMESTAMP WITHOUT TIME ZONE,
                       updated_at TIMESTAMP WITHOUT TIME ZONE,
                       last_synced_at TIMESTAMP WITHOUT TIME ZONE,
                       last_chapter_sync_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX idx_story_slug ON story (slug);
CREATE INDEX idx_story_updated_at ON story (updated_at DESC);

-- Bảng trung gian Story <-> Category (N:N) - Có ID riêng
CREATE TABLE story_category (
                                id BIGSERIAL PRIMARY KEY,
                                story_id VARCHAR(255) REFERENCES story(id) ON DELETE CASCADE,
                                category_id VARCHAR(255) REFERENCES category(id) ON DELETE CASCADE,
                                CONSTRAINT uq_story_id_category_id UNIQUE (story_id, category_id)
);
CREATE INDEX idx_story_category_story ON story_category (story_id);

-- Bảng Chapter (Danh sách chương)
CREATE TABLE chapter (
                         id VARCHAR(255) PRIMARY KEY,
                         story_id VARCHAR(255) REFERENCES story(id) ON DELETE CASCADE,
                         chapter_name VARCHAR(255) NOT NULL,
                         chapter_title VARCHAR(255),
                         chapter_api_data VARCHAR(255) UNIQUE NOT NULL,
                         is_content_synced BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP WITHOUT TIME ZONE,
                         updated_at TIMESTAMP WITHOUT TIME ZONE,
                         UNIQUE (story_id, chapter_name)
);
CREATE INDEX idx_chapter_story ON chapter (story_id);

-- Bảng Chapter Image (Nội dung ảnh)
CREATE TABLE chapter_image (
                               id BIGSERIAL PRIMARY KEY,
                               chapter_id VARCHAR(255) REFERENCES chapter(id) ON DELETE CASCADE,
                               domain_cdn VARCHAR(255) NOT NULL,
                               chapter_path VARCHAR(255) NOT NULL,
                               image_page INTEGER NOT NULL,
                               image_file VARCHAR(255) NOT NULL,
                               is_local_cached BOOLEAN DEFAULT FALSE,
                               UNIQUE (chapter_id, image_page)
);

-- ==========================================
-- 2. NHÓM NGƯỜI DÙNG (USER & AUTH)
-- ==========================================

-- Bảng Users
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       email VARCHAR(100) UNIQUE,
                       user_status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
                       created_at TIMESTAMP WITHOUT TIME ZONE,
                       updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX idx_user_username ON users (username);

-- Bảng Role (Vai trò)
CREATE TABLE role (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(50) UNIQUE NOT NULL,
                      description VARCHAR(255)
);

-- Bảng trung gian User <-> Role (N:N) - Có ID riêng
CREATE TABLE user_role (
                           id SERIAL PRIMARY KEY,
                           user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                           role_id INTEGER REFERENCES role(id) ON DELETE CASCADE,
                           CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
);

-- Bảng User Follow (Theo dõi lẫn nhau)
CREATE TABLE user_follow (
                             id BIGSERIAL PRIMARY KEY,
                             follower_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                             followed_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                             followed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT uq_follower_id_followed_id UNIQUE (follower_id, followed_id),
                             CONSTRAINT check_not_self_follow CHECK (follower_id <> followed_id)
);

-- ==========================================
-- 3. NHÓM TƯƠNG TÁC (INTERACTION)
-- ==========================================

-- Bảng Subscribe (Theo dõi truyện)
CREATE TABLE story_subscription (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                    story_id VARCHAR(255) REFERENCES story(id) ON DELETE CASCADE,
                                    created_at TIMESTAMP WITHOUT TIME ZONE,
                                    updated_at TIMESTAMP WITHOUT TIME ZONE,
                                    CONSTRAINT uq_subscription_story_id_user_id UNIQUE (story_id, user_id)
);

-- Bảng Like Story (Yêu thích truyện)
CREATE TABLE story_like (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                            story_id VARCHAR(255) REFERENCES story(id) ON DELETE CASCADE,
                            created_at TIMESTAMP WITHOUT TIME ZONE,
                            updated_at TIMESTAMP WITHOUT TIME ZONE,
                            CONSTRAINT uq_story_id_user_id UNIQUE (story_id, user_id)
);

-- Bảng Comment (Bình luận & Reply)
CREATE TABLE comment (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                         chapter_id VARCHAR(255) REFERENCES chapter(id) ON DELETE CASCADE,
                         parent_id BIGINT REFERENCES comment(id) ON DELETE CASCADE,
                         content TEXT NOT NULL,
                         is_deleted BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP WITHOUT TIME ZONE,
                         updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX idx_comment_chapter ON comment (chapter_id);

-- Bảng Comment Vote (Like/Dislike bình luận)
CREATE TABLE comment_vote (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                              comment_id BIGINT REFERENCES comment(id) ON DELETE CASCADE,
                              vote_type SMALLINT NOT NULL CHECK (vote_type IN (1, -1)),
                              voted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT uq_comment_vote_user_comment UNIQUE (user_id, comment_id)
);

-- Bảng Reading History (Lịch sử đọc) - Có ID riêng
CREATE TABLE reading_history (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                 story_id VARCHAR(255) REFERENCES story(id) ON DELETE CASCADE,
                                 last_chapter_id VARCHAR(255) REFERENCES chapter(id) ON DELETE SET NULL,
                                 created_at TIMESTAMP WITHOUT TIME ZONE,
                                 updated_at TIMESTAMP WITHOUT TIME ZONE,
                                 CONSTRAINT uq_reading_history UNIQUE (user_id, story_id)
);
CREATE INDEX idx_reading_history_user_updated ON reading_history (user_id, updated_at DESC);

-- ==========================================
-- 4. NHÓM HỆ THỐNG (SYSTEM)
-- ==========================================

-- Bảng Notice (Thông báo hệ thống)
CREATE TABLE notice (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        content TEXT NOT NULL,
                        is_active BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP WITHOUT TIME ZONE,
                        updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX idx_notice_created_at ON notice (created_at DESC);