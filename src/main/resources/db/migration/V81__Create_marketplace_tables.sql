-- 商品（Product）テーブル
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    seller_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    sold_at TIMESTAMP,
    image_urls TEXT,
    category VARCHAR(100),
    stock INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT check_price_positive CHECK (price >= 0),
    CONSTRAINT check_stock_non_negative CHECK (stock >= 0)
);

-- 注文（Order）テーブル
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    seller_id BIGINT NOT NULL REFERENCES users(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    total_amount DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2) NOT NULL,
    seller_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_PAYMENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    shipping_address TEXT,
    shipping_postal_code VARCHAR(20),
    shipping_recipient_name VARCHAR(255),
    shipping_phone_number VARCHAR(20),
    cancellation_reason TEXT,
    CONSTRAINT check_total_amount_positive CHECK (total_amount >= 0),
    CONSTRAINT check_platform_fee_non_negative CHECK (platform_fee >= 0),
    CONSTRAINT check_seller_amount_non_negative CHECK (seller_amount >= 0)
);

-- 取引（Transaction）テーブル
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    escrow_started_at TIMESTAMP,
    escrow_released_at TIMESTAMP,
    refunded_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    notes TEXT,
    CONSTRAINT check_amount_non_negative CHECK (amount >= 0)
);

-- 決済（Payment）テーブル
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    external_payment_id VARCHAR(255) UNIQUE,
    external_charge_id VARCHAR(255),
    external_transfer_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    metadata TEXT,
    CONSTRAINT check_payment_amount_positive CHECK (amount > 0)
);

-- インデックス作成（パフォーマンス向上）
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_created_at ON products(created_at DESC);

CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_product_id ON orders(product_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

CREATE INDEX idx_transactions_order_id ON transactions(order_id);
CREATE INDEX idx_transactions_status ON transactions(status);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_external_payment_id ON payments(external_payment_id);
CREATE INDEX idx_payments_status ON payments(status);
