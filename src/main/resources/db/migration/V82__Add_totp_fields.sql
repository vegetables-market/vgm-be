-- TOTP二要素認証用のフィールドを追加
ALTER TABLE users ADD COLUMN totp_secret VARCHAR(32);
ALTER TABLE users ADD COLUMN totp_enabled BOOLEAN DEFAULT FALSE;

-- インデックス追加（パフォーマンス向上）
CREATE INDEX idx_users_totp_enabled ON users(totp_enabled);
