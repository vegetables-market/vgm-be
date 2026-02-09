/*
  Guest Session Support
  - Create table t_guest_sessions
  - Create table t_cart_items
  - Modify table t_items_likes (Allow null user_id, add guest_id)
*/

-- -----------------------------------------------------
-- 1. Create t_guest_sessions
-- -----------------------------------------------------
CREATE TABLE t_guest_sessions (
    f_guest_id VARCHAR(36) PRIMARY KEY, -- UUID string
    f_expires_at TIMESTAMP NOT NULL,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_guest_sessions IS 'ゲストセッション（一時的なユーザー）';
CREATE TRIGGER set_timestamp_t_guest_sessions BEFORE UPDATE ON t_guest_sessions FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 2. Create t_cart_items
-- -----------------------------------------------------
CREATE TABLE t_cart_items (
    f_cart_item_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER,
    f_guest_id VARCHAR(36),
    f_item_id BIGINT NOT NULL,
    f_quantity INTEGER NOT NULL DEFAULT 1,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_cart_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_guest FOREIGN KEY (f_guest_id) REFERENCES t_guest_sessions (f_guest_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id) ON DELETE CASCADE,
    
    -- Either user_id or guest_id must be present
    CONSTRAINT chk_cart_owner CHECK (
        (f_user_id IS NOT NULL AND f_guest_id IS NULL) OR 
        (f_user_id IS NULL AND f_guest_id IS NOT NULL)
    ),
    
    -- Quantity check
    CONSTRAINT chk_cart_qty CHECK (f_quantity > 0)
);

COMMENT ON TABLE t_cart_items IS 'ショッピングカート';
CREATE TRIGGER set_timestamp_t_cart_items BEFORE UPDATE ON t_cart_items FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

-- Unique Index for User
CREATE UNIQUE INDEX idx_cart_user_item ON t_cart_items (f_user_id, f_item_id) WHERE f_user_id IS NOT NULL;

-- Unique Index for Guest
CREATE UNIQUE INDEX idx_cart_guest_item ON t_cart_items (f_guest_id, f_item_id) WHERE f_guest_id IS NOT NULL;


-- -----------------------------------------------------
-- 3. Modify t_items_likes
-- -----------------------------------------------------
-- Add guest_id column
ALTER TABLE t_items_likes ADD COLUMN f_guest_id VARCHAR(36);

-- Make user_id nullable
ALTER TABLE t_items_likes ALTER COLUMN f_user_id DROP NOT NULL;

-- Add FK for guest_id
ALTER TABLE t_items_likes ADD CONSTRAINT fk_likes_guest 
    FOREIGN KEY (f_guest_id) REFERENCES t_guest_sessions (f_guest_id) ON DELETE CASCADE;

-- Add check constraint (Either user or guest)
ALTER TABLE t_items_likes ADD CONSTRAINT chk_likes_owner CHECK (
    (f_user_id IS NOT NULL AND f_guest_id IS NULL) OR 
    (f_user_id IS NULL AND f_guest_id IS NOT NULL)
);

-- Existing Unique Index: t_items_likes_f_user_id_f_item_id_key usually exists from "UNIQUE (f_user_id, f_item_id)"
-- We need to check if existing constraint allows NULLs or if we should replace it with partial index.
-- Standard SQL UNIQUE allows multiple NULLs. So (NULL, 1) and (NULL, 1) might be allowed depending on DB.
-- PostgreSQL treats NULLs as distinct for UNIQUE constraints. So we should likely replace it or rely on partial index.

-- It is safer to drop the old unique constraint/index and use partial indexes for clarity and strictness on "Not Null" parts.
-- Assuming the old constraint name is `t_items_likes_f_user_id_f_item_id_key` (default naming) or explicitly named in V1.
-- In V1: UNIQUE (f_user_id, f_item_id) inside CREATE TABLE usually creates a constraint.

ALTER TABLE t_items_likes DROP CONSTRAINT IF EXISTS t_items_likes_f_user_id_f_item_id_key;

-- Create Partial Indexes
CREATE UNIQUE INDEX idx_likes_user_item ON t_items_likes (f_user_id, f_item_id) WHERE f_user_id IS NOT NULL;
CREATE UNIQUE INDEX idx_likes_guest_item ON t_items_likes (f_guest_id, f_item_id) WHERE f_guest_id IS NOT NULL;
