-- -----------------------------------------------------
-- Add display id to items for public item identifier
-- -----------------------------------------------------
ALTER TABLE t_items
    ADD COLUMN f_display_id VARCHAR(32);

-- Backfill existing rows with deterministic unique ids.
UPDATE t_items
SET f_display_id = 'G' || LPAD(f_item_id::text, 8, '0')
WHERE f_display_id IS NULL;

ALTER TABLE t_items
    ALTER COLUMN f_display_id SET NOT NULL;

ALTER TABLE t_items
    ADD CONSTRAINT uq_items_display_id UNIQUE (f_display_id);

CREATE INDEX idx_items_display_id ON t_items(f_display_id);
