-- -----------------------------------------------------
-- 14. 注文情報 (t_orders)
-- 役割: 1回の決済トランザクション。配送先スナップショットを持つ。
-- -----------------------------------------------------
CREATE TABLE t_orders (
    f_order_id BIGSERIAL PRIMARY KEY,
    f_buyer_id INTEGER NOT NULL,
    f_total_amount DECIMAL(12,0) NOT NULL, -- 決済総額
    f_status SMALLINT NOT NULL DEFAULT 1, -- 1:未払い, 2:支払い済, 9:キャンセル

    -- 配送先スナップショット (購入時点の住所を固定)
    f_shipping_name VARCHAR(100) NOT NULL,
    f_shipping_zip_code VARCHAR(10) NOT NULL,
    f_shipping_prefecture VARCHAR(20) NOT NULL,
    f_shipping_city VARCHAR(50) NOT NULL,
    f_shipping_address_line1 VARCHAR(255) NOT NULL,
    f_shipping_address_line2 VARCHAR(255),

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_buyer FOREIGN KEY (f_buyer_id) REFERENCES m_users (f_user_id)
);
CREATE TRIGGER set_timestamp_t_orders BEFORE UPDATE ON t_orders FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 16. 配送情報 (t_shipments)
-- 役割: 出品者ごとの配送単位。Amazon方式の要。
-- -----------------------------------------------------
CREATE TABLE t_shipments (
    f_shipment_id BIGSERIAL PRIMARY KEY,
    f_order_id BIGINT NOT NULL,
    f_seller_id INTEGER NOT NULL,
    f_shipping_method_id INTEGER NOT NULL,

    f_tracking_number VARCHAR(100),
    f_shipping_fee INTEGER DEFAULT 0,
    f_status SMALLINT DEFAULT 1, -- 1:発送待ち, 2:発送済み, 3:受取完了

    f_shipped_at TIMESTAMP, -- 発送日時 (TINYINTから修正済)
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shipments_order FOREIGN KEY (f_order_id) REFERENCES t_orders (f_order_id) ON DELETE CASCADE,
    CONSTRAINT fk_shipments_seller FOREIGN KEY (f_seller_id) REFERENCES m_users (f_user_id)
);
CREATE TRIGGER set_timestamp_t_shipments BEFORE UPDATE ON t_shipments FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 15. 注文商品明細 (t_orders_items)
-- 役割: 注文された個々の商品。Shipmentに紐づく。
-- -----------------------------------------------------
CREATE TABLE t_orders_items (
    f_order_item_id BIGSERIAL PRIMARY KEY,
    f_order_id BIGINT NOT NULL,
    f_shipment_id BIGINT NOT NULL,
    f_item_id BIGINT NOT NULL,
    f_seller_id INTEGER NOT NULL,

    f_unit_price INTEGER NOT NULL, -- 購入時の単価
    f_quantity INTEGER DEFAULT 1 NOT NULL,
    f_platform_fee INTEGER DEFAULT 0,
    f_seller_amount INTEGER DEFAULT 0, -- 出品者受取額

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_order FOREIGN KEY (f_order_id) REFERENCES t_orders (f_order_id),
    CONSTRAINT fk_items_shipment FOREIGN KEY (f_shipment_id) REFERENCES t_shipments (f_shipment_id),
    CONSTRAINT fk_items_item FOREIGN KEY (f_item_id) REFERENCES t_items (f_item_id)
);
CREATE TRIGGER set_timestamp_t_orders_items BEFORE UPDATE ON t_orders_items FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 17. 決済履歴 (t_payments)
-- -----------------------------------------------------
CREATE TABLE t_payments (
    f_payment_id BIGSERIAL PRIMARY KEY,
    f_order_id BIGINT NOT NULL,
    f_method VARCHAR(50), -- card, paypay
    f_ext_trans_id VARCHAR(255), -- Stripe PaymentIntent ID etc
    f_status VARCHAR(50), -- succeeded, pending
    f_amount DECIMAL(12,0),
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_order FOREIGN KEY (f_order_id) REFERENCES t_orders (f_order_id)
);


-- -----------------------------------------------------
-- 18. 資金移動/売上台帳 (t_transactions)
-- -----------------------------------------------------
CREATE TABLE t_transactions (
    f_transaction_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL, -- 対象ユーザー
    f_order_item_id BIGINT, -- 関連する注文明細(売上の場合)

    f_type VARCHAR(20) NOT NULL, -- SALES, WITHDRAWAL, REFUND
    f_amount INTEGER NOT NULL,
    f_status VARCHAR(20) NOT NULL, -- PENDING, AVAILABLE

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trans_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id),
    CONSTRAINT fk_trans_item FOREIGN KEY (f_order_item_id) REFERENCES t_orders_items (f_order_item_id)
);
CREATE TRIGGER set_timestamp_t_transactions BEFORE UPDATE ON t_transactions FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();



