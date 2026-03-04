ALTER TABLE t_items
    ADD COLUMN IF NOT EXISTS f_shipping_origin_address_id INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_items_shipping_origin_address'
          AND table_name = 't_items'
    ) THEN
        ALTER TABLE t_items
            ADD CONSTRAINT fk_items_shipping_origin_address
            FOREIGN KEY (f_shipping_origin_address_id)
            REFERENCES t_user_address (f_address_id)
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_items_shipping_origin_address
    ON t_items (f_shipping_origin_address_id);
