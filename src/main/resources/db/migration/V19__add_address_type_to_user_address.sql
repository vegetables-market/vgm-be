ALTER TABLE t_user_address
    ADD COLUMN IF NOT EXISTS f_address_type VARCHAR(20);

UPDATE t_user_address
SET f_address_type = 'DELIVERY'
WHERE f_address_type IS NULL;

ALTER TABLE t_user_address
    ALTER COLUMN f_address_type SET DEFAULT 'DELIVERY';

ALTER TABLE t_user_address
    ALTER COLUMN f_address_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_user_address_user_type_active
    ON t_user_address (f_user_id, f_address_type, f_is_default, f_updated_at)
    WHERE f_deleted_at IS NULL;
