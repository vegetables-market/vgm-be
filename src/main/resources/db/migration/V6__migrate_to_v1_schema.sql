-- V6: V1テーブルを拡張し、現行機能（認証、TOTP、決済、エスクロー）をサポート

-- =====================================================
-- 1. m_users テーブルへのカラム追加
-- =====================================================
ALTER TABLE m_users ADD COLUMN IF NOT EXISTS f_email VARCHAR(255);
ALTER TABLE m_users ADD COLUMN IF NOT EXISTS f_totp_secret VARCHAR(32);

-- インデックス追加
CREATE INDEX IF NOT EXISTS idx_m_users_username ON m_users(f_username);
CREATE INDEX IF NOT EXISTS idx_m_users_email ON m_users(f_email);
CREATE INDEX IF NOT EXISTS idx_m_users_two_factor_verified ON m_users(f_two_factor_verified);

-- =====================================================
-- 2. t_items テーブルへのカラム追加
-- =====================================================
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_description TEXT;
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_price DECIMAL(10, 2);
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_status VARCHAR(50) DEFAULT 'AVAILABLE';
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_updated_at TIMESTAMP;
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_sold_at TIMESTAMP;
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_image_urls TEXT;
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_category VARCHAR(100);
ALTER TABLE t_items ADD COLUMN IF NOT EXISTS f_stock INT DEFAULT 1;

-- インデックス追加
CREATE INDEX IF NOT EXISTS idx_t_items_user_id ON t_items(f_user_id);
CREATE INDEX IF NOT EXISTS idx_t_items_status ON t_items(f_status);
CREATE INDEX IF NOT EXISTS idx_t_items_created_at ON t_items(f_created_at DESC);

-- =====================================================
-- 3. t_orders テーブルへのカラム追加
-- =====================================================
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_seller_id BIGINT;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_item_id BIGINT;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_total_amount DECIMAL(10, 2);
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_platform_fee DECIMAL(10, 2);
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_seller_amount DECIMAL(10, 2);
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_status VARCHAR(50) DEFAULT 'PENDING_PAYMENT';
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_paid_at TIMESTAMP;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_shipped_at TIMESTAMP;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_delivered_at TIMESTAMP;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_completed_at TIMESTAMP;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_cancelled_at TIMESTAMP;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_shipping_address TEXT;
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_shipping_postal_code VARCHAR(20);
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_shipping_recipient_name VARCHAR(255);
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_shipping_phone_number VARCHAR(20);
ALTER TABLE t_orders ADD COLUMN IF NOT EXISTS f_cancellation_reason TEXT;

-- インデックス追加
CREATE INDEX IF NOT EXISTS idx_t_orders_buyer_id ON t_orders(f_buyer_id);
CREATE INDEX IF NOT EXISTS idx_t_orders_seller_id ON t_orders(f_seller_id);
CREATE INDEX IF NOT EXISTS idx_t_orders_item_id ON t_orders(f_item_id);
CREATE INDEX IF NOT EXISTS idx_t_orders_status ON t_orders(f_status);
CREATE INDEX IF NOT EXISTS idx_t_orders_created_at ON t_orders(f_created_at DESC);

-- =====================================================
-- 4. t_payments テーブル新規作成
-- =====================================================
CREATE TABLE IF NOT EXISTS t_payments (
    f_payment_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    f_order_id BIGINT NOT NULL,
    f_user_id INT NOT NULL,
    f_payment_method VARCHAR(50) NOT NULL,
    f_amount DECIMAL(10, 2) NOT NULL,
    f_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    f_external_payment_id VARCHAR(255) UNIQUE,
    f_external_charge_id VARCHAR(255),
    f_external_transfer_id VARCHAR(255),
    f_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_completed_at TIMESTAMP,
    f_failed_at TIMESTAMP,
    f_error_message TEXT,
    f_metadata TEXT,
    CONSTRAINT chk_payment_amount_positive CHECK (f_amount > 0)
);

-- インデックス追加
CREATE INDEX IF NOT EXISTS idx_t_payments_order_id ON t_payments(f_order_id);
CREATE INDEX IF NOT EXISTS idx_t_payments_user_id ON t_payments(f_user_id);
CREATE INDEX IF NOT EXISTS idx_t_payments_external_payment_id ON t_payments(f_external_payment_id);
CREATE INDEX IF NOT EXISTS idx_t_payments_status ON t_payments(f_status);

-- =====================================================
-- 5. t_transactions テーブル新規作成
-- =====================================================
CREATE TABLE IF NOT EXISTS t_transactions (
    f_transaction_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    f_order_id BIGINT NOT NULL UNIQUE,
    f_type VARCHAR(50) NOT NULL,
    f_amount DECIMAL(10, 2) NOT NULL,
    f_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    f_escrow_started_at TIMESTAMP,
    f_escrow_released_at TIMESTAMP,
    f_refunded_at TIMESTAMP,
    f_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP,
    f_notes TEXT,
    CONSTRAINT chk_transaction_amount_non_negative CHECK (f_amount >= 0)
);

-- インデックス追加
CREATE INDEX IF NOT EXISTS idx_t_transactions_order_id ON t_transactions(f_order_id);
CREATE INDEX IF NOT EXISTS idx_t_transactions_status ON t_transactions(f_status);
