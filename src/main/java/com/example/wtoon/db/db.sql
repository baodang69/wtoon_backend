-- ==========================================
-- 1. NHÓM DỮ LIỆU CỐT LÕI (CORE DATA)
-- ==========================================

-- Bảng Category (Thể loại)
CREATE TABLE category (
                          id VARCHAR(50) PRIMARY KEY, -- ID gốc từ API (VD: "65086549...")
                          name VARCHAR(100) UNIQUE NOT NULL,
                          slug VARCHAR(100) UNIQUE NOT NULL
);

-- Bảng Story (Truyện)
CREATE TABLE story (
                       id VARCHAR(50) PRIMARY KEY, -- ID gốc từ API
                       name VARCHAR(255) NOT NULL,
                       slug VARCHAR(255) UNIQUE NOT NULL,
                       status VARCHAR(50), -- ongoing, completed...
                       thumb_url VARCHAR(500),
                       cdn_domain VARCHAR(255),
                       description TEXT,
                       author VARCHAR(255),
                       view_count BIGINT DEFAULT 0 NOT NULL, -- (Mới thêm) Đếm lượt xem
                       updated_at TIMESTAMP WITHOUT TIME ZONE, -- Thời gian nguồn update
                       last_synced_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- Job 1/2 chạy
                       last_chapter_sync_at TIMESTAMP WITHOUT TIME ZONE -- Job 3 chạy
);

-- Bảng trung gian Story <-> Category (N:N)
CREATE TABLE story_category (
                                story_id VARCHAR(50) REFERENCES story(id) ON DELETE CASCADE,
                                category_id VARCHAR(50) REFERENCES category(id) ON DELETE CASCADE,
                                PRIMARY KEY (story_id, category_id)
);

-- Bảng Chapter (Danh sách chương)
CREATE TABLE chapter (
                         id VARCHAR(50) PRIMARY KEY, -- (Đã sửa) Dùng String ID từ API
                         story_id VARCHAR(50) REFERENCES story(id) ON DELETE CASCADE,
                         chapter_name VARCHAR(255) NOT NULL,
                         chapter_title VARCHAR(255),
                         chapter_api_data VARCHAR(500) UNIQUE NOT NULL, -- Link API lấy ảnh
                         is_content_synced BOOLEAN DEFAULT FALSE, -- Cờ đánh dấu đã lấy ảnh chưa
                         created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         UNIQUE (story_id, chapter_name)
);

-- Bảng Chapter Image (Nội dung ảnh - On Demand)
CREATE TABLE chapter_image (
                               id BIGSERIAL PRIMARY KEY,
                               chapter_id VARCHAR(50) REFERENCES chapter(id) ON DELETE CASCADE,
                               domain_cdn VARCHAR(255) NOT NULL,
                               chapter_path VARCHAR(500) NOT NULL,
                               image_page INT NOT NULL,
                               image_file VARCHAR(255) NOT NULL,
                               is_local_cached BOOLEAN DEFAULT FALSE,
                               UNIQUE (chapter_id, image_page)
);

-- ==========================================
-- 2. NHÓM NGƯỜI DÙNG (USER & AUTH)
-- ==========================================

-- Bảng User
CREATE TABLE "user" (
                        id BIGSERIAL PRIMARY KEY,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        password_hash VARCHAR(100) NOT NULL,
                        email VARCHAR(100) UNIQUE,
                        role VARCHAR(50) DEFAULT 'user', -- 'user', 'admin', 'author'
                        created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng User Follow (Follow tác giả/người dùng khác)
CREATE TABLE user_follow (
                             follower_id BIGINT REFERENCES "user"(id) ON DELETE CASCADE,
                             followed_id BIGINT REFERENCES "user"(id) ON DELETE CASCADE,
                             followed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (follower_id, followed_id),
                             CHECK (follower_id <> followed_id)
);

-- ==========================================
-- 3. NHÓM TƯƠNG TÁC (INTERACTION)
-- ==========================================

-- Bảng Subscribe (Đăng ký nhận thông báo truyện)
CREATE TABLE story_subscription (
                                    user_id BIGINT REFERENCES "user"(id) ON DELETE CASCADE,
                                    story_id VARCHAR(50) REFERENCES story(id) ON DELETE CASCADE,
                                    subscribed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (user_id, story_id)
);

-- Bảng Like Story (Yêu thích truyện) - (Mới thêm)
CREATE TABLE story_like (
                            user_id BIGINT REFERENCES "user"(id) ON DELETE CASCADE,
                            story_id VARCHAR(50) REFERENCES story(id) ON DELETE CASCADE,
                            liked_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (user_id, story_id)
);

-- Bảng Comment (Bình luận cấp 1)
CREATE TABLE comment (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT REFERENCES "user"(id) ON DELETE SET NULL,
                         chapter_id VARCHAR(50) REFERENCES chapter(id) ON DELETE CASCADE,
                         content TEXT NOT NULL,
                         created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         is_deleted BOOLEAN DEFAULT FALSE
);

-- Bảng Comment Reply (Trả lời bình luận)
CREATE TABLE comment_reply (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT REFERENCES "user"(id) ON DELETE SET NULL,
                               comment_id BIGINT REFERENCES comment(id) ON DELETE CASCADE,
                               content TEXT NOT NULL,
                               created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               is_deleted BOOLEAN DEFAULT FALSE
);

-- Bảng Comment Vote (Like/Dislike bình luận) - (Mới thêm)
CREATE TABLE comment_vote (
                              user_id BIGINT REFERENCES "user"(id) ON DELETE CASCADE,
                              comment_id BIGINT REFERENCES comment(id) ON DELETE CASCADE,
                              vote_type SMALLINT NOT NULL CHECK (vote_type IN (1, -1)), -- 1: Like, -1: Dislike
                              voted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (user_id, comment_id)
);

-- ==========================================
-- 4. NHÓM HỆ THỐNG (SYSTEM)
-- ==========================================

-- Bảng Notice (Thông báo hệ thống)
CREATE TABLE notice (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        content TEXT NOT NULL,
                        created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        is_active BOOLEAN DEFAULT TRUE
);