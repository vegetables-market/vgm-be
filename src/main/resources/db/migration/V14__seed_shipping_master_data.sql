-- -----------------------------------------------------
-- Seed shipping master data used by item draft/publish flows
-- -----------------------------------------------------

-- 配送方法マスタ
INSERT INTO m_shipping_method (f_shipping_method_id, f_name, f_is_anonymous, f_is_tracking, f_allows_cool)
SELECT 1, '未定', 0, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM m_shipping_method WHERE f_shipping_method_id = 1
);

INSERT INTO m_shipping_method (f_shipping_method_id, f_name, f_is_anonymous, f_is_tracking, f_allows_cool)
SELECT 2, '通常配送', 0, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM m_shipping_method WHERE f_shipping_method_id = 2
);

INSERT INTO m_shipping_method (f_shipping_method_id, f_name, f_is_anonymous, f_is_tracking, f_allows_cool)
SELECT 3, 'クール便', 0, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM m_shipping_method WHERE f_shipping_method_id = 3
);

-- 発送日数マスタ
INSERT INTO m_shipping_days (f_shipping_days_id, f_name, f_min_days, f_max_days)
SELECT 1, '1~2日で発送', 1, 2
WHERE NOT EXISTS (
    SELECT 1 FROM m_shipping_days WHERE f_shipping_days_id = 1
);

INSERT INTO m_shipping_days (f_shipping_days_id, f_name, f_min_days, f_max_days)
SELECT 2, '2~3日で発送', 2, 3
WHERE NOT EXISTS (
    SELECT 1 FROM m_shipping_days WHERE f_shipping_days_id = 2
);

INSERT INTO m_shipping_days (f_shipping_days_id, f_name, f_min_days, f_max_days)
SELECT 3, '4~7日で発送', 4, 7
WHERE NOT EXISTS (
    SELECT 1 FROM m_shipping_days WHERE f_shipping_days_id = 3
);

-- SERIAL採番の整合を合わせる
SELECT setval('m_shipping_method_f_shipping_method_id_seq', COALESCE((SELECT MAX(f_shipping_method_id) FROM m_shipping_method), 1), true);
SELECT setval('m_shipping_days_f_shipping_days_id_seq', COALESCE((SELECT MAX(f_shipping_days_id) FROM m_shipping_days), 1), true);
