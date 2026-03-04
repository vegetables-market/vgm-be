ALTER TABLE t_user_address
    ADD COLUMN IF NOT EXISTS f_deleted_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_user_address_user_active
    ON t_user_address (f_user_id, f_is_default, f_updated_at)
    WHERE f_deleted_at IS NULL;
