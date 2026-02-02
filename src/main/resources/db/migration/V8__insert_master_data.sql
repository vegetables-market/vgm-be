-- -----------------------------------------------------
-- マスターデータ投入
-- -----------------------------------------------------

-- 発送日数マスタ (m_shipping_days)
INSERT INTO m_shipping_days (f_shipping_days_id, f_name, f_min_days, f_max_days) VALUES
(1, '1~2日で発送', 1, 2),
(2, '2~3日で発送', 2, 3),
(3, '4~7日で発送', 4, 7)
ON CONFLICT (f_shipping_days_id) DO NOTHING;

-- 配送方法マスタ (m_shipping_method)
INSERT INTO m_shipping_method (f_shipping_method_id, f_name, f_is_anonymous, f_is_tracking, f_allows_cool) VALUES
(1, 'らくらくメルカリ便', 1, 1, 0),
(2, 'ゆうゆうメルカリ便', 1, 1, 0),
(3, 'ゆうメール', 0, 0, 0),
(4, 'クリックポスト', 0, 1, 0),
(5, 'ゆうパック', 0, 1, 1),
(6, '宅急便', 0, 1, 1),
(7, '普通郵便', 0, 0, 0),
(8, '未定', 0, 0, 0)
ON CONFLICT (f_shipping_method_id) DO NOTHING;

-- カテゴリマスタ (m_categories) - 基本カテゴリ
INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order) VALUES
(1, '野菜', NULL, 1, 1),
(2, '果物', NULL, 1, 2),
(3, '米・穀類', NULL, 1, 3),
(4, '肉類', NULL, 1, 4),
(5, '魚介類', NULL, 1, 5),
(6, '加工食品', NULL, 1, 6),
(7, 'その他', NULL, 1, 99)
ON CONFLICT (f_category_id) DO NOTHING;

-- シーケンスを更新
SELECT setval('m_shipping_days_f_shipping_days_id_seq', (SELECT MAX(f_shipping_days_id) FROM m_shipping_days));
SELECT setval('m_shipping_method_f_shipping_method_id_seq', (SELECT MAX(f_shipping_method_id) FROM m_shipping_method));
SELECT setval('m_categories_f_category_id_seq', (SELECT MAX(f_category_id) FROM m_categories));
