-- V1: エンティティに基づくテーブル定義

-- =====================================================
-- 1. m_users (ユーザー)
-- =====================================================
CREATE TABLE m_users (
    f_user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    f_username VARCHAR(100) NOT NULL UNIQUE,
    f_password_hash VARCHAR(255) NOT NULL,
    f_email VARCHAR(255),
    f_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_totp_secret VARCHAR(32),
    f_two_factor_verified BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_m_users_username ON m_users(f_username);
CREATE INDEX idx_m_users_email ON m_users(f_email);

-- =====================================================
-- 2. t_items (商品)
-- =====================================================
CREATE TABLE t_items (
    f_item_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    f_item_name VARCHAR(255) NOT NULL,
    f_description TEXT,
    f_price DECIMAL(10, 2) NOT NULL,
    f_user_id INT NOT NULL REFERENCES m_users(f_user_id),
    f_status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    f_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP,
    f_sold_at TIMESTAMP,
    f_image_urls TEXT,
    f_category VARCHAR(100),
    f_stock INT NOT NULL DEFAULT 1
);

CREATE INDEX idx_t_items_user_id ON t_items(f_user_id);
CREATE INDEX idx_t_items_status ON t_items(f_status);
CREATE INDEX idx_t_items_created_at ON t_items(f_created_at DESC);

-- =====================================================
-- 3. t_orders (注文)
-- =====================================================
CREATE TABLE t_orders (
    f_order_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    f_buyer_id INT NOT NULL REFERENCES m_users(f_user_id),
    f_seller_id INT NOT NULL REFERENCES m_users(f_user_id),
    f_item_id BIGINT NOT NULL REFERENCES t_items(f_item_id),
    f_total_amount DECIMAL(10, 2) NOT NULL,
    f_platform_fee DECIMAL(10, 2) NOT NULL,
    f_seller_amount DECIMAL(10, 2) NOT NULL,
    f_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_PAYMENT',
    f_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    f_paid_at TIMESTAMP,
    f_shipped_at TIMESTAMP,
    f_delivered_at TIMESTAMP,
    f_completed_at TIMESTAMP,
    f_cancelled_at TIMESTAMP,
    f_shipping_address TEXT,
    f_shipping_postal_code VARCHAR(20),
    f_shipping_recipient_name VARCHAR(255),
    f_shipping_phone_number VARCHAR(20),
    f_cancellation_reason TEXT
);

CREATE INDEX idx_t_orders_buyer_id ON t_orders(f_buyer_id);
CREATE INDEX idx_t_orders_seller_id ON t_orders(f_seller_id);
CREATE INDEX idx_t_orders_item_id ON t_orders(f_item_id);
CREATE INDEX idx_t_orders_status ON t_orders(f_status);
CREATE INDEX idx_t_orders_created_at ON t_orders(f_created_at DESC);

-- =====================================================
-- 4. t_payments (決済)
-- =====================================================
CREATE TABLE t_payments (
    f_payment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    f_order_id BIGINT NOT NULL REFERENCES t_orders(f_order_id),
    f_user_id INT NOT NULL REFERENCES m_users(f_user_id),
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

CREATE INDEX idx_t_payments_order_id ON t_payments(f_order_id);
CREATE INDEX idx_t_payments_user_id ON t_payments(f_user_id);
CREATE INDEX idx_t_payments_external_payment_id ON t_payments(f_external_payment_id);
CREATE INDEX idx_t_payments_status ON t_payments(f_status);

-- =====================================================
-- 5. t_transactions (取引・エスクロー)
-- =====================================================
CREATE TABLE t_transactions (
    f_transaction_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    f_order_id BIGINT NOT NULL UNIQUE REFERENCES t_orders(f_order_id),
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

CREATE INDEX idx_t_transactions_order_id ON t_transactions(f_order_id);
CREATE INDEX idx_t_transactions_status ON t_transactions(f_status);
