/*
  System: grand market
  Database: PostgreSQL
  Version: 1.0.0
  Description: Initial Schema Creation
*/

-- -----------------------------------------------------
-- 0. 事前準備 (Extensions & Functions)
-- -----------------------------------------------------

-- PostGIS拡張の有効化（位置情報用）
CREATE EXTENSION IF NOT EXISTS postgis;

-- 更新日時(f_updated_at)を自動更新するための関数定義
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.f_updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- -----------------------------------------------------
-- 01. ユーザーマスター (m_users)
-- -----------------------------------------------------
CREATE TABLE m_users (
    f_user_id SERIAL PRIMARY KEY,
    f_username VARCHAR(100) NOT NULL UNIQUE,
    f_display_name VARCHAR(100) NOT NULL,
    f_password_hash VARCHAR(255) NOT NULL,
    f_last_login_at TIMESTAMP,
    f_status SMALLINT DEFAULT 1, -- 0:無効,1:有効,2:停止,3:削除
    f_email_verified SMALLINT DEFAULT 0,
    f_phone_verified SMALLINT DEFAULT 0,
    f_two_factor_verified SMALLINT DEFAULT 0,
    f_identity_verified SMALLINT DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
);

COMMENT ON TABLE m_users IS 'ユーザーマスター情報';
COMMENT ON COLUMN m_users.f_user_id IS 'ユーザーID';
COMMENT ON COLUMN m_users.f_username IS 'ユーザー名';
COMMENT ON COLUMN m_users.f_status IS 'ステータス(0:無効,1:有効,2:停止,3:削除)';

-- 更新日時トリガー
CREATE TRIGGER set_timestamp_m_users
BEFORE UPDATE ON m_users
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 02. ユーザー情報 (t_user_info)
-- -----------------------------------------------------
CREATE TABLE t_user_info (
    f_user_id INTEGER PRIMARY KEY,
    f_last_name VARCHAR(100),
    f_first_name VARCHAR(100),
    f_last_name_kana VARCHAR(100),
    f_first_name_kana VARCHAR(100),
    f_birth_date DATE,
    f_gender SMALLINT DEFAULT 0, -- 0:未選択,1:男性,2:女性,3:その他
    f_phone_number VARCHAR(50),
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_info_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_info IS 'ユーザー情報';
CREATE TRIGGER set_timestamp_t_user_info
BEFORE UPDATE ON t_user_info
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 03. ユーザープロフィール (t_user_profile)
-- -----------------------------------------------------
CREATE TABLE t_user_profile (
    f_user_id INTEGER PRIMARY KEY,
    f_profile_text TEXT,
    f_profile_image_url VARCHAR(500),
    f_rating_count INTEGER DEFAULT 0,
    f_rating_sum INTEGER DEFAULT 0,
    f_sales_count INTEGER DEFAULT 0,
    f_purchases_count INTEGER DEFAULT 0,
    f_following_count INTEGER DEFAULT 0,
    f_followers_count INTEGER DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profile_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_profile IS 'ユーザープロフィール';
CREATE TRIGGER set_timestamp_t_user_profile
BEFORE UPDATE ON t_user_profile
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 04. ユーザーアドレス (t_user_address)
-- -----------------------------------------------------
CREATE TABLE t_user_address (
    f_address_id SERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_postal_code VARCHAR(100) NOT NULL,
    f_prefecture VARCHAR(100) NOT NULL,
    f_city VARCHAR(100) NOT NULL,
    f_address_line1 VARCHAR(100) NOT NULL,
    f_address_line2 VARCHAR(100),
    f_country_code VARCHAR(2) DEFAULT 'JP' NOT NULL,
    f_is_default SMALLINT DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_address_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_address IS 'ユーザーアドレス';
CREATE TRIGGER set_timestamp_t_user_address
BEFORE UPDATE ON t_user_address
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 05. ユーザー決済情報 (t_user_pay_info)
-- -----------------------------------------------------
CREATE TABLE t_user_pay_info (
    f_pay_info_id SERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_pay_type SMALLINT NOT NULL,
    f_external_customer_id VARCHAR(100),
    f_external_payment_method_id VARCHAR(100),
    f_masked_info VARCHAR(20),
    f_pay_name VARCHAR(50),
    f_is_default SMALLINT DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_pay_info_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_pay_info IS 'ユーザー決済情報';
CREATE TRIGGER set_timestamp_t_user_pay_info
BEFORE UPDATE ON t_user_pay_info
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 06. ユーザー名変更履歴 (username_history)
-- -----------------------------------------------------
CREATE TABLE username_history (
    f_history_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_old_username VARCHAR(100) NOT NULL,
    f_new_username VARCHAR(100) NOT NULL,
    f_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_username_history_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE username_history IS 'ユーザー名変更履歴';


-- -----------------------------------------------------
-- 07. ユーザーフォロー情報 (t_user_follows)
-- -----------------------------------------------------
CREATE TABLE t_user_follows (
    f_follow_id BIGSERIAL PRIMARY KEY,
    f_follower_id INTEGER NOT NULL,
    f_followed_id INTEGER NOT NULL,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (f_follower_id, f_followed_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (f_follower_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_followed FOREIGN KEY (f_followed_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_follows IS 'ユーザーフォロー情報';


-- -----------------------------------------------------
-- 08. 商品カテゴリー (m_categories)
-- -----------------------------------------------------
CREATE TABLE m_categories (
    f_category_id BIGSERIAL PRIMARY KEY,
    f_parent_id BIGINT,
    f_category_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_category_parent FOREIGN KEY (f_parent_id) REFERENCES m_categories (f_category_id)
);

COMMENT ON TABLE m_categories IS 'カテゴリ情報';


-- -----------------------------------------------------
-- 11-B. 配送方法 (m_shipping_method)
-- -----------------------------------------------------
CREATE TABLE m_shipping_method (
    f_shipping_method_id SERIAL PRIMARY KEY,
    f_name VARCHAR(100) NOT NULL,
    f_is_anonymous INTEGER DEFAULT 0 NOT NULL,
    f_is_tracking INTEGER DEFAULT 0 NOT NULL,
    f_allows_cool INTEGER DEFAULT 0 NOT NULL
);

COMMENT ON TABLE m_shipping_method IS '配送方法マスタ';


-- -----------------------------------------------------
-- 09. 商品情報 (t_items)
-- -----------------------------------------------------
CREATE TABLE t_items (
    f_item_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_name VARCHAR(100) NOT NULL,
    f_description TEXT NOT NULL,
    f_categories_id BIGINT NOT NULL,
    f_price INTEGER NOT NULL,
    f_quantity INTEGER DEFAULT 1 NOT NULL,
    f_status INTEGER DEFAULT 0 NOT NULL, -- 0:下書き,1:出品中...
    f_shipping_payer_type INTEGER DEFAULT 0 NOT NULL,
    f_shipping_origin_area INTEGER NOT NULL,
    f_shipping_days_id INTEGER NOT NULL,
    f_item_condition INTEGER NOT NULL,
    f_brand VARCHAR(100),
    f_shipping_method_id INTEGER NOT NULL,
    f_preservation_method INTEGER NOT NULL, -- 0:常温,1:冷蔵,2:冷凍
    f_expiration_date TIMESTAMP,
    f_weight INTEGER,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_city_code VARCHAR(10),
    f_public_lat DOUBLE PRECISION,
    f_public_long DOUBLE PRECISION,
    f_location_precision INTEGER DEFAULT 0,
    location GEOMETRY(Point, 4326), -- PostGIS: SRID 4326 (WGS84)
    CONSTRAINT fk_items_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_items_category_id FOREIGN KEY (f_categories_id) REFERENCES m_categories (f_category_id),
    CONSTRAINT fk_items_shipping_method FOREIGN KEY (f_shipping_method_id) REFERENCES m_shipping_method (f_shipping_method_id)
);

COMMENT ON TABLE t_items IS '商品情報';
-- インデックス推奨 (PostGIS検索用)
CREATE INDEX idx_items_location ON t_items USING GIST (location);

CREATE TRIGGER set_timestamp_t_items
BEFORE UPDATE ON t_items
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 10. 商品画像 (t_items_images)
-- -----------------------------------------------------
CREATE TABLE t_items_images (
    f_image_id BIGSERIAL PRIMARY KEY,
    f_item_id BIGINT NOT NULL,
    f_image_url VARCHAR(200) NOT NULL,
    f_display_order INTEGER DEFAULT 1,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_status INTEGER,
    CONSTRAINT fk_item_images_item_id FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_items_images IS '商品画像';


-- -----------------------------------------------------
-- 16. お気に入り (t_user_favorites)
-- -----------------------------------------------------
CREATE TABLE t_user_favorites (
    f_favorite_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_item_id BIGINT NOT NULL,
    f_is_active BOOLEAN DEFAULT TRUE,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (f_user_id, f_item_id),
    CONSTRAINT fk_favorites_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_item_id FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_favorites IS 'お気に入り';


-- -----------------------------------------------------
-- 09-B. 注文情報 (t_orders)
-- -----------------------------------------------------
CREATE TABLE t_orders (
    f_order_id BIGSERIAL PRIMARY KEY,
    f_listing_id BIGINT NOT NULL,
    f_buyer_id INTEGER NOT NULL,
    f_ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_payment_id BIGINT,
    f_payment_date TIMESTAMP,
    f_amount DECIMAL(10,2),
    f_payment_method VARCHAR(20),
    f_payment_status VARCHAR(20),
    CONSTRAINT fk_orders_listing_id FOREIGN KEY (f_listing_id) REFERENCES t_items (f_item_id),
    CONSTRAINT fk_orders_buyer_id FOREIGN KEY (f_buyer_id) REFERENCES m_users (f_user_id)
);

COMMENT ON TABLE t_orders IS '注文情報';


-- -----------------------------------------------------
-- 11-A. レビュー情報 (t_reviews)
-- -----------------------------------------------------
CREATE TABLE t_reviews (
    f_review_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_item_id BIGINT NOT NULL,
    f_rating SMALLINT NOT NULL,
    f_comment TEXT,
    f_posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id),
    CONSTRAINT fk_reviews_item_id FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id)
);

COMMENT ON TABLE t_reviews IS 'レビュー情報';
CREATE TRIGGER set_timestamp_t_reviews
BEFORE UPDATE ON t_reviews
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 12. ユーザーメッセージ (t_messages)
-- -----------------------------------------------------
CREATE TABLE t_messages (
    f_message_id BIGSERIAL PRIMARY KEY,
    f_sender_id INTEGER NOT NULL,
    f_receiver_id INTEGER NOT NULL,
    f_thread_id BIGINT,
    f_message_content TEXT,
    f_create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_read_status SMALLINT DEFAULT 0,
    CONSTRAINT fk_messages_sender FOREIGN KEY (f_sender_id) REFERENCES m_users (f_user_id),
    CONSTRAINT fk_messages_receiver FOREIGN KEY (f_receiver_id) REFERENCES m_users (f_user_id)
);

COMMENT ON TABLE t_messages IS 'ユーザーメッセージ';
CREATE TRIGGER set_timestamp_t_messages
BEFORE UPDATE ON t_messages
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 13. コメント情報 (t_comments)
-- -----------------------------------------------------
CREATE TABLE t_comments (
    f_comment_id BIGSERIAL PRIMARY KEY,
    f_listing_id BIGINT NOT NULL,
    f_user_id INTEGER NOT NULL,
    f_comment_content TEXT NOT NULL,
    f_create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_read_status SMALLINT DEFAULT 0,
    CONSTRAINT fk_comments_listing FOREIGN KEY (f_listing_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id)
);

COMMENT ON TABLE t_comments IS 'コメント情報';
CREATE TRIGGER set_timestamp_t_comments
BEFORE UPDATE ON t_comments
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 14. 配送情報 (t_shipments)
-- -----------------------------------------------------
CREATE TABLE t_shipments (
    f_shipments_id BIGSERIAL PRIMARY KEY,
    f_order_id BIGINT NOT NULL,
    f_carrier INTEGER,
    tracking_no VARCHAR(50),
    f_postal_code VARCHAR(8) NOT NULL,
    f_address VARCHAR(100) NOT NULL,
    f_delivery_status INTEGER DEFAULT 0,
    f_shipped_at TIMESTAMP,
    f_delivered_at TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shipments_order FOREIGN KEY (f_order_id) REFERENCES t_orders (f_order_id)
);

COMMENT ON TABLE t_shipments IS '配送情報';
CREATE TRIGGER set_timestamp_t_shipments
BEFORE UPDATE ON t_shipments
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 17. 都道府県の位置情報データ (t_Prefecture_L)
-- -----------------------------------------------------
CREATE TABLE t_Prefecture_L (
    f_prefecture_id BIGINT NOT NULL PRIMARY KEY,
    f_prefecture_jpname VARCHAR(10) NOT NULL,
    f_latitude_longitude VARCHAR(50) NOT NULL,
    f_romaj VARCHAR(30) NOT NULL,
    f_address_id INTEGER
);

COMMENT ON TABLE t_Prefecture_L IS '都道府県の位置情報データ';