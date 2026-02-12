/*
  System: grand market
  Database: PostgreSQL 16+
  Version: 1.0.0
  Description: Initial Schema Creation with PostGIS & Amazon-style Shipping & Auth Enhancements (Unified V1)
*/

-- -----------------------------------------------------
-- 0. 事前準備 (Extensions & Functions)
-- -----------------------------------------------------

-- PostGIS拡張の有効化（位置情報・距離計算用）
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- 更新日時(f_updated_at)を自動更新するための関数定義
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.f_updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;