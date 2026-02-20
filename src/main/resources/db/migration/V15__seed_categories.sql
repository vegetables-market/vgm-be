-- -----------------------------------------------------
-- Seed category master data
-- -----------------------------------------------------

-- Root categories
INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 1, '野菜', NULL, 1, 10
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 1);

INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 2, '果物', NULL, 1, 20
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 2);

INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 3, '米・穀物', NULL, 1, 30
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 3);

-- Children: 野菜
INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 101, '葉物野菜', 1, 2, 10
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 101);

INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 102, '根菜', 1, 2, 20
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 102);

INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 103, 'きのこ', 1, 2, 30
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 103);

-- Children: 果物
INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 201, '柑橘類', 2, 2, 10
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 201);

INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 202, 'ベリー類', 2, 2, 20
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 202);

INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 203, 'りんご・梨', 2, 2, 30
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 203);

-- Children: 米・穀物
INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 301, '白米', 3, 2, 10
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 301);

INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 302, '玄米', 3, 2, 20
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 302);

INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order)
SELECT 303, '雑穀', 3, 2, 30
WHERE NOT EXISTS (SELECT 1 FROM m_categories WHERE f_category_id = 303);

SELECT setval('m_categories_f_category_id_seq', COALESCE((SELECT MAX(f_category_id) FROM m_categories), 1), true);
