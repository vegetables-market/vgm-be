/*
  V3: Enable Draft Items by dropping NOT NULL constraints
*/

-- Allow NULL for name (Draft items may not have a title yet)
ALTER TABLE t_items ALTER COLUMN f_name DROP NOT NULL;

-- Allow NULL for description
ALTER TABLE t_items ALTER COLUMN f_description DROP NOT NULL;

-- Allow NULL for category
ALTER TABLE t_items ALTER COLUMN f_categories_id DROP NOT NULL;

-- Allow NULL for price
ALTER TABLE t_items ALTER COLUMN f_price DROP NOT NULL;

-- Allow NULL for shipping details
ALTER TABLE t_items ALTER COLUMN f_shipping_origin_area DROP NOT NULL;
ALTER TABLE t_items ALTER COLUMN f_shipping_days_id DROP NOT NULL;
ALTER TABLE t_items ALTER COLUMN f_shipping_method_id DROP NOT NULL;

-- Note: f_status, f_user_id should remain NOT NULL.
