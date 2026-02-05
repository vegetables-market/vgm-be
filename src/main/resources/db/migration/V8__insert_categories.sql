-- カテゴリー初期データ投入

-- ルートカテゴリー
INSERT INTO m_categories (f_category_id, f_name, f_level, f_sort_order, f_icon_url) VALUES 
(1, 'レディース', 1, 1, '👩'),
(2, 'メンズ', 1, 2, '👨'),
(3, 'ベビー・キッズ', 1, 3, '👶'),
(4, 'インテリア・住まい・小物', 1, 4, '🏠'),
(5, '本・音楽・ゲーム', 1, 5, '📚'),
(6, 'おもちゃ・ホビー・グッズ', 1, 6, '🧸'),
(7, 'コスメ・香水・美容', 1, 7, '💄'),
(8, '家電・スマホ・カメラ', 1, 8, '📱'),
(9, 'スポーツ・レジャー', 1, 9, '⚽'),
(10, 'ハンドメイド', 1, 10, '🧶')
ON CONFLICT (f_category_id) DO NOTHING;

-- 子カテゴリー: レディース (ID: 1)
INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order) VALUES
(101, 'トップス', 1, 2, 1),
(102, 'ジャケット/アウター', 1, 2, 2),
(103, 'パンツ', 1, 2, 3),
(104, 'スカート', 1, 2, 4),
(105, 'ワンピース', 1, 2, 5),
(106, '靴', 1, 2, 6)
ON CONFLICT (f_category_id) DO NOTHING;

-- 子カテゴリー: メンズ (ID: 2)
INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order) VALUES
(201, 'トップス', 2, 2, 1),
(202, 'ジャケット/アウター', 2, 2, 2),
(203, 'パンツ', 2, 2, 3),
(204, '靴', 2, 2, 4),
(205, 'バッグ', 2, 2, 5)
ON CONFLICT (f_category_id) DO NOTHING;

-- 子カテゴリー: ゲーム (ID: 5)
INSERT INTO m_categories (f_category_id, f_name, f_parent_id, f_level, f_sort_order) VALUES
(501, 'テレビゲーム', 5, 2, 1),
(502, 'PCゲーム', 5, 2, 2),
(503, '本', 5, 2, 3),
(504, 'CD', 5, 2, 4),
(505, 'DVD/ブルーレイ', 5, 2, 5)
ON CONFLICT (f_category_id) DO NOTHING;

-- シーケンスを更新して、ID重複エラーを防ぐ
SELECT setval('m_categories_f_category_id_seq', (SELECT MAX(f_category_id) FROM m_categories));
