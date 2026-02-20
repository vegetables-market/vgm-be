BEGIN;

-- =========================
-- 共通: updated_at 自動更新
-- =========================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.f_updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =========================
-- ユーザーマスタ
-- =========================
CREATE TABLE m_users (
    f_user_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    f_username       VARCHAR(100) NOT NULL UNIQUE,
    f_display_name   VARCHAR(100) NOT NULL,
    f_password_hash  VARCHAR(255) NOT NULL,

    f_last_login_at  TIMESTAMP,
    f_status         SMALLINT NOT NULL DEFAULT 1,

    f_email_verified SMALLINT NOT NULL DEFAULT 0,
    f_phone_verified SMALLINT NOT NULL DEFAULT 0,
    f_two_factor_verified SMALLINT NOT NULL DEFAULT 0,
    f_identity_verified SMALLINT NOT NULL DEFAULT 0,

    f_created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER trg_users_updated
BEFORE UPDATE ON m_users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================
-- ユーザー基本情報
-- =========================
CREATE TABLE t_user_info (
    f_user_id        BIGINT PRIMARY KEY,
    f_last_name      VARCHAR(100),
    f_first_name     VARCHAR(100),
    f_last_name_kana VARCHAR(100),
    f_first_name_kana VARCHAR(100),
    f_birth_date     DATE,
    f_gender         SMALLINT DEFAULT 0,
    f_phone_number   VARCHAR(50),

    f_created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_info_user
        FOREIGN KEY (f_user_id)
        REFERENCES m_users(f_user_id)
        ON DELETE CASCADE
);

CREATE TRIGGER trg_user_info_updated
BEFORE UPDATE ON t_user_info
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================
-- ユーザープロフィール
-- =========================
CREATE TABLE t_user_profile (
    f_user_id        BIGINT PRIMARY KEY,
    f_profile_text   TEXT,
    f_profile_image_url VARCHAR(500),

    f_rating_count   INT NOT NULL DEFAULT 0,
    f_rating_sum     INT NOT NULL DEFAULT 0,
    f_sales_count    INT NOT NULL DEFAULT 0,
    f_purchases_count INT NOT NULL DEFAULT 0,
    f_following_count INT NOT NULL DEFAULT 0,
    f_followers_count INT NOT NULL DEFAULT 0,

    f_created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_profile_user
        FOREIGN KEY (f_user_id)
        REFERENCES m_users(f_user_id)
        ON DELETE CASCADE
);

CREATE TRIGGER trg_user_profile_updated
BEFORE UPDATE ON t_user_profile
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================
-- ユーザー住所
-- =========================
CREATE TABLE t_user_address (
    f_address_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    f_user_id        BIGINT NOT NULL,

    f_postal_code    VARCHAR(20) NOT NULL,
    f_prefecture     VARCHAR(50) NOT NULL,
    f_city           VARCHAR(100) NOT NULL,
    f_address_line1  VARCHAR(100) NOT NULL,
    f_address_line2  VARCHAR(100),
    f_country_code   VARCHAR(2) NOT NULL DEFAULT 'JP',
    f_is_default     SMALLINT NOT NULL DEFAULT 0,

    f_created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_address_user
        FOREIGN KEY (f_user_id)
        REFERENCES m_users(f_user_id)
        ON DELETE CASCADE
);

CREATE TRIGGER trg_user_address_updated
BEFORE UPDATE ON t_user_address
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================
-- コメント
-- =========================
CREATE TABLE t_comments (
    f_comment_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    f_listing_id     BIGINT,
    f_user_id        BIGINT NOT NULL,

    f_comment_content TEXT NOT NULL,
    f_read_status     SMALLINT DEFAULT 0,

    f_created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_user
        FOREIGN KEY (f_user_id)
        REFERENCES m_users(f_user_id)
        ON DELETE CASCADE
);

CREATE TRIGGER trg_comments_updated
BEFORE UPDATE ON t_comments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================
-- 都道府県マスタ
-- =========================
CREATE TABLE m_prefecture (
    f_prefecture_id  SMALLINT PRIMARY KEY,
    f_prefecture_jpname VARCHAR(10) NOT NULL,
    f_romaji         VARCHAR(30) NOT NULL,
    f_latitude       NUMERIC(9,6),
    f_longitude      NUMERIC(9,6)
);

COMMIT;
