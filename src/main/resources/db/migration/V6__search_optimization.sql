/*
  System: grand market
  Database: PostgreSQL 16+
  Version: 6
  Description: Search Optimization - Add indexes and likes count
*/

-- インデックス追加（検索最適化）
CREATE INDEX IF NOT EXISTS idx_items_title ON t_items USING gin(to_tsvector('simple', f_name));
CREATE INDEX IF NOT EXISTS idx_items_price ON t_items(f_price);
CREATE INDEX IF NOT EXISTS idx_items_category ON t_items(f_categories_id);
CREATE INDEX IF NOT EXISTS idx_items_status ON t_items(f_status);
CREATE INDEX IF NOT EXISTS idx_items_created_at ON t_items(f_created_at DESC);

-- お気に入り数カラム追加
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_likes_count INTEGER DEFAULT 0;

-- 既存データのお気に入り数を初期化
UPDATE t_items
SET f_likes_count = (
    SELECT COUNT(*)
    FROM t_items_likes
    WHERE t_items_likes.f_item_id = t_items.f_item_id
);

-- お気に入り数を更新するトリガー関数
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

-- トリガー作成
DROP TRIGGER IF EXISTS trigger_update_likes_count ON t_items_likes;
CREATE TRIGGER trigger_update_likes_count
AFTER INSERT OR DELETE ON t_items_likes
FOR EACH ROW EXECUTE FUNCTION update_item_likes_count();
