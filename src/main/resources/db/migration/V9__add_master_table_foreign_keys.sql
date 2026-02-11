-- -----------------------------------------------------
-- Add foreign keys that depend on master tables created in V7
-- -----------------------------------------------------

ALTER TABLE t_items
    ADD CONSTRAINT fk_items_spot FOREIGN KEY (f_spot_id) REFERENCES m_spots (f_spot_id),
    ADD CONSTRAINT fk_items_shipping_days FOREIGN KEY (f_shipping_days_id) REFERENCES m_shipping_days (f_shipping_days_id),
    ADD CONSTRAINT fk_items_shipping_method FOREIGN KEY (f_shipping_method_id) REFERENCES m_shipping_method (f_shipping_method_id);

ALTER TABLE t_shipments
    ADD CONSTRAINT fk_shipments_method FOREIGN KEY (f_shipping_method_id) REFERENCES m_shipping_method (f_shipping_method_id);

