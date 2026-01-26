-- -----------------------------------------------------
-- 29. ユーザーセッション管理 (t_user_sessions)
-- 役割: ログイン中の端末管理、セッション維持
-- -----------------------------------------------------
CREATE TABLE t_user_sessions (
    f_session_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,

    -- セッション識別子（Cookieに保存する値。連番IDの代わりにUUID等を使用）
    f_session_key VARCHAR(255) NOT NULL UNIQUE,

    -- リフレッシュトークン（ハッシュ化して保存）
    f_refresh_token_hash VARCHAR(255) NOT NULL,

    -- 端末情報
    f_device_name VARCHAR(100), -- 例: "Chrome on MacOS"
    f_ip_address VARCHAR(45),   -- IPv6対応

    -- 状態管理
    f_is_revoked BOOLEAN DEFAULT FALSE, -- TRUEなら無効化済み

    -- 有効期限
    f_expires_at TIMESTAMP NOT NULL,

    -- タイムスタンプ
    f_last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sessions_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

-- インデックス
CREATE INDEX idx_sessions_key ON t_user_sessions (f_session_key);
CREATE INDEX idx_sessions_user ON t_user_sessions (f_user_id);
CREATE INDEX idx_sessions_token ON t_user_sessions (f_refresh_token_hash);

COMMENT ON TABLE t_user_sessions IS 'ユーザーセッション管理';
COMMENT ON COLUMN t_user_sessions.f_session_key IS 'Cookie照合用ランダムキー';
COMMENT ON COLUMN t_user_sessions.f_is_revoked IS 'セッション無効化フラグ';
