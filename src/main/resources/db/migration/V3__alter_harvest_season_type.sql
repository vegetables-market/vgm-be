-- Change harvest season column to timestamp
-- For PostgreSQL
ALTER TABLE t_items ALTER COLUMN f_harvest_season TYPE timestamp USING f_harvest_season::timestamp;

-- Note: Ensure existing values are compatible or clean them before running in production.
