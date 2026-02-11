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
    f_status SMALLINT DEFAULT 1 NOT NULL, -- 0:下書き, 1:出品中, 2:取引中, 3:売切, 4:停止

    f_shipping_payer_type SMALLINT DEFAULT 0, -- 0:送料込(出品者負担), 1:着払い
    f_shipping_origin_area INTEGER, -- 都道府県ID (検索用テキストインデックス代わり)
    f_shipping_days_id INTEGER,
    f_shipping_method_id INTEGER,

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
CREATE TRIGGER set_timestamp_t_reviews BEFORE UPDATE ON t_reviews FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

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


