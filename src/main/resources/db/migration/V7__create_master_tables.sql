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
    f_location GEOGRAPHY(POINT, 4326) NOT NULL,

    -- 0:正確(ピン), 1:あいまい(円), 2:都道府県/市のみ
    f_disclosure_type SMALLINT NOT NULL DEFAULT 0,
    f_fuzzy_radius INTEGER DEFAULT 500, -- あいまい時の半径(m)

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spots_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
-- 空間インデックス（検索高速化）
CREATE INDEX idx_spots_location ON m_spots USING GIST (f_location);
CREATE TRIGGER set_timestamp_m_spots BEFORE UPDATE ON m_spots FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();