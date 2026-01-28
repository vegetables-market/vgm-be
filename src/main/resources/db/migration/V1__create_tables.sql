/*
  System: grand market
  Database: PostgreSQL 16+
  Version: 1.0.0
  Description: Initial Schema Creation with PostGIS & Amazon-style Shipping & Auth Enhancements (Unified V1)
*/

-- -----------------------------------------------------
-- 0. 事前準備 (Extensions & Functions)
-- -----------------------------------------------------

-- PostGIS拡張の有効化（位置情報・距離計算用）
-- CREATE EXTENSION IF NOT EXISTS postgis;

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
    f_email VARCHAR(255) UNIQUE, -- V2 added
    f_password_hash VARCHAR(255) NOT NULL,
    f_last_login_at TIMESTAMP,
    f_status SMALLINT DEFAULT 1, -- 0:無効,1:仮登録,2:有効,3:停止,4:削除
    
    -- Verification Flags
    f_email_verified SMALLINT DEFAULT 0,
    f_phone_verified SMALLINT DEFAULT 0,
    f_two_factor_verified SMALLINT DEFAULT 0,
    f_identity_verified SMALLINT DEFAULT 0,
    
    -- MFA Settings (V5, V6 added)
    f_is_mfa_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    f_preferred_mfa_type VARCHAR(20) DEFAULT NULL, -- 'TOTP', 'EMAIL', etc.

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE m_users IS 'ユーザーマスター';
COMMENT ON COLUMN m_users.f_email IS 'メールアドレス';
COMMENT ON COLUMN m_users.f_preferred_mfa_type IS '優先MFA方式 (TOTP, EMAIL, etc). NULLなら無効';
CREATE TRIGGER set_timestamp_m_users BEFORE UPDATE ON m_users FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


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
    CONSTRAINT fk_user_info_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
COMMENT ON TABLE t_user_info IS 'ユーザー詳細情報';
CREATE TRIGGER set_timestamp_t_user_info BEFORE UPDATE ON t_user_info FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


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
    CONSTRAINT fk_user_profile_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
COMMENT ON TABLE t_user_profile IS 'ユーザープロフィール';
CREATE TRIGGER set_timestamp_t_user_profile BEFORE UPDATE ON t_user_profile FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 04. ユーザーアドレス (t_user_address)
-- -----------------------------------------------------
CREATE TABLE t_user_address (
    f_address_id SERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_postal_code VARCHAR(8) NOT NULL,
    f_prefecture VARCHAR(50) NOT NULL,
    f_city VARCHAR(100) NOT NULL,
    f_address_line1 VARCHAR(255) NOT NULL,
    f_address_line2 VARCHAR(255),
    f_country_code VARCHAR(2) DEFAULT 'JP',
    f_is_default SMALLINT DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_address_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
COMMENT ON TABLE t_user_address IS 'ユーザー住所情報';
CREATE TRIGGER set_timestamp_t_user_address BEFORE UPDATE ON t_user_address FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 05. ユーザー決済情報 (t_user_pay_info)
-- -----------------------------------------------------
CREATE TABLE t_user_pay_info (
    f_pay_info_id SERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_pay_type SMALLINT NOT NULL, -- 1:Card, 2:PayPay etc
    f_external_customer_id VARCHAR(100), -- Stripe Customer ID
    f_external_payment_method_id VARCHAR(100),
    f_masked_info VARCHAR(50), -- ****-****-****-1234
    f_is_default SMALLINT DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_pay_info_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
COMMENT ON TABLE t_user_pay_info IS 'ユーザー決済情報';
CREATE TRIGGER set_timestamp_t_user_pay_info BEFORE UPDATE ON t_user_pay_info FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 06. ユーザー名変更履歴 (t_username_history)
-- -----------------------------------------------------
CREATE TABLE t_username_history (
    f_history_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_old_username VARCHAR(100) NOT NULL,
    f_new_username VARCHAR(100) NOT NULL,
    f_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);


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


-- -----------------------------------------------------
-- 24. 都道府県マスタ (m_prefectures)
-- -----------------------------------------------------
CREATE TABLE m_prefectures (
    f_prefecture_id INTEGER PRIMARY KEY,
    f_name VARCHAR(20) NOT NULL
);
-- 初期データ投入（必要なら）
INSERT INTO m_prefectures (f_prefecture_id, f_name) VALUES
(1, '北海道'), (2, '青森県'), (3, '岩手県'), (4, '宮城県'), (5, '秋田県'),
(6, '山形県'), (7, '福島県'), (8, '茨城県'), (9, '栃木県'), (10, '群馬県'),
(11, '埼玉県'), (12, '千葉県'), (13, '東京都'), (14, '神奈川県'), (15, '新潟県'),
(16, '富山県'), (17, '石川県'), (18, '福井県'), (19, '山梨県'), (20, '長野県'),
(21, '岐阜県'), (22, '静岡県'), (23, '愛知県'), (24, '三重県'), (25, '滋賀県'),
(26, '京都府'), (27, '大阪府'), (28, '兵庫県'), (29, '奈良県'), (30, '和歌山県'),
(31, '鳥取県'), (32, '島根県'), (33, '岡山県'), (34, '広島県'), (35, '山口県'),
(36, '徳島県'), (37, '香川県'), (38, '愛媛県'), (39, '高知県'), (40, '福岡県'),
(41, '佐賀県'), (42, '長崎県'), (43, '熊本県'), (44, '大分県'), (45, '宮崎県'),
(46, '鹿児島県'), (47, '沖縄県');


-- -----------------------------------------------------
-- 08. 商品カテゴリー (m_categories)
-- -----------------------------------------------------
CREATE TABLE m_categories (
    f_category_id BIGSERIAL PRIMARY KEY,
    f_name VARCHAR(100) NOT NULL,
    f_parent_id BIGINT,
    f_level INTEGER DEFAULT 1,
    f_icon_url VARCHAR(500),
    f_sort_order INTEGER DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_parent FOREIGN KEY (f_parent_id) REFERENCES m_categories (f_category_id)
);
CREATE TRIGGER set_timestamp_m_categories BEFORE UPDATE ON m_categories FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 13. 配送方法マスタ (m_shipping_method)
-- -----------------------------------------------------
CREATE TABLE m_shipping_method (
    f_shipping_method_id SERIAL PRIMARY KEY,
    f_name VARCHAR(100) NOT NULL,
    f_is_anonymous SMALLINT DEFAULT 0, -- 0:通常, 1:匿名
    f_is_tracking SMALLINT DEFAULT 0,
    f_allows_cool SMALLINT DEFAULT 0
);


-- -----------------------------------------------------
-- 20. 発送日数マスタ (m_shipping_days)
-- -----------------------------------------------------
CREATE TABLE m_shipping_days (
    f_shipping_days_id SERIAL PRIMARY KEY,
    f_name VARCHAR(50) NOT NULL, -- "1~2日で発送"
    f_min_days INTEGER NOT NULL,
    f_max_days INTEGER NOT NULL,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- -----------------------------------------------------
-- 19. 場所・拠点マスター (m_spots)
-- PostGISを使用。Map検索の核となるテーブル
-- -----------------------------------------------------
CREATE TABLE m_spots (
    f_spot_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_name VARCHAR(100), -- 拠点名（例: 第1農園）

    -- 地球球面座標系 (SRID=4326: WGS84)
    location GEOGRAPHY(POINT, 4326) NOT NULL,

    -- 0:正確(ピン), 1:あいまい(円), 2:都道府県/市のみ
    disclosure_type SMALLINT NOT NULL DEFAULT 0,
    fuzzy_radius INTEGER DEFAULT 500, -- あいまい時の半径(m)

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spots_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
-- 空間インデックス（検索高速化）
CREATE INDEX idx_spots_location ON m_spots USING GIST (location);
CREATE TRIGGER set_timestamp_m_spots BEFORE UPDATE ON m_spots FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 10. 商品情報 (t_items)
-- -----------------------------------------------------
CREATE TABLE t_items (
    f_item_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_spot_id BIGINT, -- 発送元・生産地拠点

    f_name VARCHAR(100) NOT NULL,
    f_description TEXT NOT NULL,
    f_categories_id BIGINT NOT NULL,
    f_price INTEGER NOT NULL,
    f_quantity INTEGER DEFAULT 1 NOT NULL,
    f_status SMALLINT DEFAULT 1 NOT NULL, -- 0:下書き, 1:出品中, 2:取引中, 3:売切, 4:停止

    f_shipping_payer_type SMALLINT DEFAULT 0, -- 0:送料込(出品者負担), 1:着払い
    f_shipping_origin_area INTEGER NOT NULL, -- 都道府県ID (検索用テキストインデックス代わり)
    f_shipping_days_id INTEGER NOT NULL,
    f_shipping_method_id INTEGER NOT NULL,

    f_item_condition SMALLINT DEFAULT 0, -- 0:新品...
    f_preservation_method SMALLINT DEFAULT 0, -- 0:常温, 1:冷蔵, 2:冷凍
    f_expiration_date TIMESTAMP,
    f_brand VARCHAR(100),
    f_weight INTEGER, -- グラム単位

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_items_spot FOREIGN KEY (f_spot_id) REFERENCES m_spots (f_spot_id),
    CONSTRAINT fk_items_category FOREIGN KEY (f_categories_id) REFERENCES m_categories (f_category_id),
    CONSTRAINT fk_items_shipping_days FOREIGN KEY (f_shipping_days_id) REFERENCES m_shipping_days (f_shipping_days_id),
    CONSTRAINT fk_items_shipping_method FOREIGN KEY (f_shipping_method_id) REFERENCES m_shipping_method (f_shipping_method_id)
);
CREATE TRIGGER set_timestamp_t_items BEFORE UPDATE ON t_items FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 12. 商品画像 (t_items_images)
-- -----------------------------------------------------
CREATE TABLE t_items_images (
    f_image_id BIGSERIAL PRIMARY KEY,
    f_item_id BIGINT NOT NULL,
    f_image_url TEXT NOT NULL,
    f_display_order INTEGER DEFAULT 1,
    f_status SMALLINT DEFAULT 1, -- 1:有効, 0:無効
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_images_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE
);


-- -----------------------------------------------------
-- 11. 商品お気に入り (t_items_likes)
-- -----------------------------------------------------
CREATE TABLE t_items_likes (
    f_item_favorite_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_item_id BIGINT NOT NULL,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (f_user_id, f_item_id),
    CONSTRAINT fk_likes_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE
);


-- -----------------------------------------------------
-- 14. 注文情報 (t_orders)
-- 役割: 1回の決済トランザクション。配送先スナップショットを持つ。
-- -----------------------------------------------------
CREATE TABLE t_orders (
    f_order_id BIGSERIAL PRIMARY KEY,
    f_buyer_id INTEGER NOT NULL,
    f_total_amount DECIMAL(12,0) NOT NULL, -- 決済総額
    f_status SMALLINT NOT NULL DEFAULT 1, -- 1:未払い, 2:支払い済, 9:キャンセル

    -- 配送先スナップショット (購入時点の住所を固定)
    f_shipping_name VARCHAR(100) NOT NULL,
    f_shipping_zip_code VARCHAR(10) NOT NULL,
    f_shipping_prefecture VARCHAR(20) NOT NULL,
    f_shipping_city VARCHAR(50) NOT NULL,
    f_shipping_address_line1 VARCHAR(255) NOT NULL,
    f_shipping_address_line2 VARCHAR(255),

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_buyer FOREIGN KEY (f_buyer_id) REFERENCES m_users (f_user_id)
);
CREATE TRIGGER set_timestamp_t_orders BEFORE UPDATE ON t_orders FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 16. 配送情報 (t_shipments)
-- 役割: 出品者ごとの配送単位。Amazon方式の要。
-- -----------------------------------------------------
CREATE TABLE t_shipments (
    f_shipment_id BIGSERIAL PRIMARY KEY,
    f_order_id BIGINT NOT NULL,
    f_seller_id INTEGER NOT NULL,
    f_shipping_method_id INTEGER NOT NULL,

    f_tracking_number VARCHAR(100),
    f_shipping_fee INTEGER DEFAULT 0,
    f_status SMALLINT DEFAULT 1, -- 1:発送待ち, 2:発送済み, 3:受取完了

    f_shipped_at TIMESTAMP, -- 発送日時 (TINYINTから修正済)
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shipments_order FOREIGN KEY (f_order_id) REFERENCES t_orders (f_order_id) ON DELETE CASCADE,
    CONSTRAINT fk_shipments_seller FOREIGN KEY (f_seller_id) REFERENCES m_users (f_user_id),
    CONSTRAINT fk_shipments_method FOREIGN KEY (f_shipping_method_id) REFERENCES m_shipping_method (f_shipping_method_id)
);
CREATE TRIGGER set_timestamp_t_shipments BEFORE UPDATE ON t_shipments FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 15. 注文商品明細 (t_orders_items)
-- 役割: 注文された個々の商品。Shipmentに紐づく。
-- -----------------------------------------------------
CREATE TABLE t_orders_items (
    f_order_item_id BIGSERIAL PRIMARY KEY,
    f_order_id BIGINT NOT NULL,
    f_shipment_id BIGINT NOT NULL,
    f_item_id BIGINT NOT NULL,
    f_seller_id INTEGER NOT NULL,

    f_unit_price INTEGER NOT NULL, -- 購入時の単価
    f_quantity INTEGER DEFAULT 1 NOT NULL,
    f_platform_fee INTEGER DEFAULT 0,
    f_seller_amount INTEGER DEFAULT 0, -- 出品者受取額

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_order FOREIGN KEY (f_order_id) REFERENCES t_orders (f_order_id),
    CONSTRAINT fk_items_shipment FOREIGN KEY (f_shipment_id) REFERENCES t_shipments (f_shipment_id),
    CONSTRAINT fk_items_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id)
);
CREATE TRIGGER set_timestamp_t_orders_items BEFORE UPDATE ON t_orders_items FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 17. 決済履歴 (t_payments)
-- -----------------------------------------------------
CREATE TABLE t_payments (
    f_payment_id BIGSERIAL PRIMARY KEY,
    f_order_id BIGINT NOT NULL,
    f_method VARCHAR(50), -- card, paypay
    f_ext_trans_id VARCHAR(255), -- Stripe PaymentIntent ID etc
    f_status VARCHAR(50), -- succeeded, pending
    f_amount DECIMAL(12,0),
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_order FOREIGN KEY (f_order_id) REFERENCES t_orders (f_order_id)
);


-- -----------------------------------------------------
-- 18. 資金移動/売上台帳 (t_transactions)
-- -----------------------------------------------------
CREATE TABLE t_transactions (
    f_transaction_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL, -- 対象ユーザー
    f_order_item_id BIGINT, -- 関連する注文明細(売上の場合)

    f_type VARCHAR(20) NOT NULL, -- SALES, WITHDRAWAL, REFUND
    f_amount INTEGER NOT NULL,
    f_status VARCHAR(20) NOT NULL, -- PENDING, AVAILABLE

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trans_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id),
    CONSTRAINT fk_trans_item FOREIGN KEY (f_order_item_id) REFERENCES t_orders_items (f_order_item_id)
);
CREATE TRIGGER set_timestamp_t_transactions BEFORE UPDATE ON t_transactions FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 21. レビュー情報 (t_reviews)
-- -----------------------------------------------------
CREATE TABLE t_reviews (
    f_review_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL, -- レビューを書いた人
    f_item_id BIGINT NOT NULL,
    f_rating SMALLINT NOT NULL,
    f_comment TEXT,
    f_posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id)
);
CREATE TRIGGER set_timestamp_t_reviews BEFORE UPDATE ON t_reviews FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 22. ユーザーメッセージ (t_messages)
-- -----------------------------------------------------
CREATE TABLE t_messages (
    f_message_id BIGSERIAL PRIMARY KEY,
    f_sender_id INTEGER NOT NULL,
    f_receiver_id INTEGER NOT NULL,
    f_item_id BIGINT, -- どの商品に関する問い合わせか(任意)

    f_message_content TEXT,
    f_image_url TEXT, -- 型修正済み
    f_read_status SMALLINT DEFAULT 0, -- 0:未読, 1:既読

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_msg_sender FOREIGN KEY (f_sender_id) REFERENCES m_users (f_user_id),
    CONSTRAINT fk_msg_receiver FOREIGN KEY (f_receiver_id) REFERENCES m_users (f_user_id)
);
CREATE TRIGGER set_timestamp_t_messages BEFORE UPDATE ON t_messages FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 23. 商品コメント (t_item_comment)
-- -----------------------------------------------------
CREATE TABLE t_item_comment (
    f_item_comment_id BIGSERIAL PRIMARY KEY,
    f_item_id BIGINT NOT NULL,
    f_user_id INTEGER NOT NULL,
    f_comment_content TEXT NOT NULL,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
CREATE TRIGGER set_timestamp_t_item_comment BEFORE UPDATE ON t_item_comment FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 25. 2段階認証設定 (t_user_two_factor)
-- セキュリティ情報を分離管理するために作成
-- -----------------------------------------------------
CREATE TABLE t_user_two_factor (
    f_two_factor_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL UNIQUE, -- 1ユーザー1レコード

    -- TOTPシークレット (Base32)。暗号化して保存するのが理想だが、まずは平文でも分離されていればOK
    f_secret_key VARCHAR(255) NOT NULL,

    -- バックアップコード (カンマ区切りやJSONで保存)
    f_backup_codes TEXT,

    -- 設定が有効かどうか
    f_is_enabled BOOLEAN DEFAULT FALSE,

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_2fa_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_two_factor IS '2段階認証のシークレット情報';
CREATE TRIGGER set_timestamp_t_user_two_factor BEFORE UPDATE ON t_user_two_factor FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 26. 一時的検証コード (t_verification_codes)
-- メール認証、SMS認証、パスワードリセット用
-- -----------------------------------------------------
CREATE TABLE t_verification_codes (
    f_code_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER, -- 未登録ユーザーの場合はNULLもありえる
    f_email VARCHAR(255), -- 送信先

    f_code VARCHAR(50) NOT NULL, -- "123456" など
    f_type VARCHAR(50) NOT NULL, -- "EMAIL_VERIFY", "PASSWORD_RESET", "2FA_SMS"
    f_flow_id VARCHAR(255) UNIQUE, -- V4 added

    f_expires_at TIMESTAMP NOT NULL, -- 有効期限 (発行から10分後など)
    f_is_used BOOLEAN DEFAULT FALSE, -- 使用済みか

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 期限切れデータの掃除をしやすくするためのインデックス
CREATE INDEX idx_verify_code_email ON t_verification_codes (f_email, f_code, f_type);
CREATE INDEX idx_verify_flow_id ON t_verification_codes (f_flow_id); -- V4 added
COMMENT ON COLUMN t_verification_codes.f_flow_id IS '認証フロー識別用UUID';


-- -----------------------------------------------------
-- 27. アプリバージョン管理 (m_app_versions)
-- -----------------------------------------------------
CREATE TABLE m_app_versions (
    f_version_id BIGSERIAL PRIMARY KEY,
    f_platform VARCHAR(20) NOT NULL DEFAULT 'pwa',
    f_version_name VARCHAR(20) NOT NULL, -- '1.2.0'
    f_version_code INTEGER NOT NULL,     -- 120

    -- 最低稼働バージョン (これ未満は強制アップデート対象)
    f_min_supported_version INTEGER NOT NULL DEFAULT 0,

    f_released_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 公開日時
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE m_app_versions IS 'アプリバージョン管理';
CREATE TRIGGER set_timestamp_m_app_versions BEFORE UPDATE ON m_app_versions FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 28. アプリ更新内容 (m_app_update_notes)
-- -----------------------------------------------------
CREATE TABLE m_app_update_notes (
    f_update_note_id BIGSERIAL PRIMARY KEY,
    f_version_id BIGINT NOT NULL,

    f_title VARCHAR(100) NOT NULL,
    f_message TEXT NOT NULL,

    -- 重要度 (UI表示用: 'HIGH', 'NORMAL' など)
    f_importance VARCHAR(20) DEFAULT 'NORMAL',

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notes_version FOREIGN KEY (f_version_id) REFERENCES m_app_versions (f_version_id) ON DELETE CASCADE
);
COMMENT ON TABLE m_app_update_notes IS 'アプリ更新内容詳細';
CREATE TRIGGER set_timestamp_m_app_update_notes BEFORE UPDATE ON m_app_update_notes FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 29. ユーザーセッション管理 (t_user_sessions) (V3 added)
-- 役割: ログイン中の端末管理、セッション維持
-- -----------------------------------------------------
CREATE TABLE t_user_sessions (
    f_session_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,

    -- セッション識別子（Cookieに保存する値。連番IDの代わりにUUID等を使用）
    f_session_key VARCHAR(255) NOT NULL UNIQUE,

    -- リフレッシュトークン（ハッシュ化して保存）
    f_refresh_token_hash VARCHAR(255) NOT NULL,

    -- 端末情報
    f_device_name VARCHAR(100), -- 例: "Chrome on MacOS"
    f_ip_address VARCHAR(45),   -- IPv6対応

    -- 状態管理
    f_is_revoked BOOLEAN DEFAULT FALSE, -- TRUEなら無効化済み

    -- 有効期限
    f_expires_at TIMESTAMP NOT NULL,

    -- タイムスタンプ
    f_last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sessions_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

-- インデックス
CREATE INDEX idx_sessions_key ON t_user_sessions (f_session_key);
CREATE INDEX idx_sessions_user ON t_user_sessions (f_user_id);
CREATE INDEX idx_sessions_token ON t_user_sessions (f_refresh_token_hash);

COMMENT ON TABLE t_user_sessions IS 'ユーザーセッション管理';
COMMENT ON COLUMN t_user_sessions.f_session_key IS 'Cookie照合用ランダムキー';
COMMENT ON COLUMN t_user_sessions.f_is_revoked IS 'セッション無効化フラグ';