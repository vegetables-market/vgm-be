WITH ranked_sender_addresses AS (
    SELECT
        f_address_id,
        ROW_NUMBER() OVER (
            PARTITION BY f_user_id
            ORDER BY f_updated_at DESC, f_created_at DESC, f_address_id DESC
        ) AS row_num
    FROM t_user_address
    WHERE f_address_type = 'SENDER'
      AND f_deleted_at IS NULL
)
UPDATE t_user_address ua
SET
    f_deleted_at = CURRENT_TIMESTAMP,
    f_is_default = 0
FROM ranked_sender_addresses rsa
WHERE ua.f_address_id = rsa.f_address_id
  AND rsa.row_num > 1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_address_one_active_sender_per_user
    ON t_user_address (f_user_id)
    WHERE f_address_type = 'SENDER' AND f_deleted_at IS NULL;
