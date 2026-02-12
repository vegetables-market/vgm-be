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
-- 10. 商品情報 (t_items)
-- -----------------------------------------------------
CREATE TABLE t_items (
    f_item_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_spot_id BIGINT, -- 発送元・生産地拠点

    f_name VARCHAR(100),
    f_description TEXT,
    f_categories_id BIGINT,
    f_price INTEGER,
    f_quantity INTEGER DEFAULT 1 NOT NULL,
    f_status SMALLINT DEFAULT 1 NOT NULL, -- ステータス0,出品作業中,1,下書き、2,出品中、3,取引中、4,売り切れ、5,停止

    f_shipping_payer_type SMALLINT DEFAULT 0, -- 0:送料込(出品者負担), 1:着払い
    f_shipping_origin_area INTEGER, -- 都道府県ID (検索用テキストインデックス代わり)
    f_shipping_days_id INTEGER,
    f_shipping_method_id INTEGER,

    f_item_condition SMALLINT DEFAULT 0, -- 0:新品...
    f_preservation_method SMALLINT DEFAULT 0, -- 0:常温, 1:冷蔵, 2:冷凍
    f_expiration_date TIMESTAMP,
    f_brand VARCHAR(100),
    f_weight INTEGER, -- グラム単位
    f_likes_count INTEGER DEFAULT 0, -- お気に入り数

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_items_category FOREIGN KEY (f_categories_id) REFERENCES m_categories (f_category_id)
);
CREATE TRIGGER set_timestamp_t_items BEFORE UPDATE ON t_items FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

-- 検索最適化用インデックス
CREATE INDEX idx_items_title ON t_items USING gin(to_tsvector('simple', f_name));
CREATE INDEX idx_items_price ON t_items(f_price);
CREATE INDEX idx_items_category ON t_items(f_categories_id);
CREATE INDEX idx_items_status ON t_items(f_status);
CREATE INDEX idx_items_created_at ON t_items(f_created_at DESC);


-- -----------------------------------------------------
-- 11. 商品お気に入り (t_items_likes) (V12よりゲスト対応に修正)
-- -----------------------------------------------------
CREATE TABLE t_items_likes (
    f_item_favorite_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER, -- ゲスト対応のためNULL許容に変更
    f_guest_id VARCHAR(36), -- ゲストID追加
    f_item_id BIGINT NOT NULL,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_likes_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_guest FOREIGN KEY (f_guest_id) REFERENCES t_guest_sessions (f_guest_id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE,

    -- ユーザーまたはゲストのいずれかが必須
    CONSTRAINT chk_likes_owner CHECK (
        (f_user_id IS NOT NULL AND f_guest_id IS NULL) OR
        (f_user_id IS NULL AND f_guest_id IS NOT NULL)
    )
);

-- ユニーク制約の代わりに部分インデックスを使用
CREATE UNIQUE INDEX idx_likes_user_item ON t_items_likes (f_user_id, f_item_id) WHERE f_user_id IS NOT NULL;
CREATE UNIQUE INDEX idx_likes_guest_item ON t_items_likes (f_guest_id, f_item_id) WHERE f_guest_id IS NOT NULL;


-- -----------------------------------------------------
-- 36. ショッピングカート (t_cart_items)
-- -----------------------------------------------------
CREATE TABLE t_cart_items (
    f_cart_item_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER,
    f_guest_id VARCHAR(36),
    f_item_id BIGINT NOT NULL,
    f_quantity INTEGER NOT NULL DEFAULT 1,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_cart_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_guest FOREIGN KEY (f_guest_id) REFERENCES t_guest_sessions (f_guest_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE,

    -- Either user_id or guest_id must be present
    CONSTRAINT chk_cart_owner CHECK (
        (f_user_id IS NOT NULL AND f_guest_id IS NULL) OR
        (f_user_id IS NULL AND f_guest_id IS NOT NULL)
    ),

    -- Quantity check
    CONSTRAINT chk_cart_qty CHECK (f_quantity > 0)
);

CREATE UNIQUE INDEX idx_cart_user_item ON t_cart_items (f_user_id, f_item_id) WHERE f_user_id IS NOT NULL;
CREATE UNIQUE INDEX idx_cart_guest_item ON t_cart_items (f_guest_id, f_item_id) WHERE f_guest_id IS NOT NULL;

COMMENT ON TABLE t_cart_items IS 'ショッピングカート';
CREATE TRIGGER set_timestamp_t_cart_items BEFORE UPDATE ON t_cart_items FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

-- お気に入り数を更新するトリガー関数 (V6 & V12マージ)
CREATE OR REPLACE FUNCTION update_item_likes_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE t_items SET f_likes_count = f_likes_count + 1 WHERE f_item_id = NEW.f_item_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE t_items SET f_likes_count = f_likes_count - 1 WHERE f_item_id = OLD.f_item_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_likes_count
AFTER INSERT OR DELETE ON t_items_likes
FOR EACH ROW EXECUTE FUNCTION update_item_likes_count();


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
-- 21. 商品レビュー情報 (t_item_reviews)
-- -----------------------------------------------------
CREATE TABLE t_item_reviews (
    f_item_review_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL, -- レビューを書いた人
    f_item_id BIGINT NOT NULL,
    f_rating SMALLINT NOT NULL,
    f_comment TEXT,
    f_posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id)
);
CREATE TRIGGER set_timestamp_t_item_reviews BEFORE UPDATE ON t_item_reviews FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

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


