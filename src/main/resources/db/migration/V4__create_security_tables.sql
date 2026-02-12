-- -----------------------------------------------------
-- 25. 2段階認証設定 (t_user_two_factor)
-- セキュリティ情報を分離管理するために作成
-- -----------------------------------------------------
CREATE TABLE t_user_two_factor (
    f_two_factor_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL UNIQUE, -- 1ユーザー1レコード

    -- TOTPシークレット (Base32)。暗号化して保存するのが理想だが、まずは平文でも分離されていればOK
    f_secret_key VARCHAR(255) NOT NULL,

    -- バックアップコード (カンマ区切りやJSONで保存)
    f_backup_codes TEXT,

    -- 設定が有効かどうか
    f_is_enabled BOOLEAN DEFAULT FALSE,

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_2fa_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_two_factor IS '2段階認証のシークレット情報';
CREATE TRIGGER set_timestamp_t_user_two_factor BEFORE UPDATE ON t_user_two_factor FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 26. 一時的検証コード (t_verification_codes)
-- メール認証、SMS認証、パスワードリセット用
-- -----------------------------------------------------
CREATE TABLE t_verification_codes (
    f_code_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER, -- 未登録ユーザーの場合はNULLもありえる
    f_email VARCHAR(255), -- 送信先

    f_code VARCHAR(50) NOT NULL, -- "123456" など
    f_type VARCHAR(50) NOT NULL, -- "EMAIL_VERIFY", "PASSWORD_RESET", "2FA_SMS"
    f_flow_id VARCHAR(255) UNIQUE, -- V4 added

    f_expires_at TIMESTAMP NOT NULL, -- 有効期限 (発行から10分後など)
    f_is_used BOOLEAN DEFAULT FALSE, -- 使用済みか
    f_resend_count INTEGER DEFAULT 0 NOT NULL, -- 再送回数

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 期限切れデータの掃除をしやすくするためのインデックス
CREATE INDEX idx_verify_code_email ON t_verification_codes (f_email, f_code, f_type);
CREATE INDEX idx_verify_flow_id ON t_verification_codes (f_flow_id); -- V4 added
COMMENT ON COLUMN t_verification_codes.f_flow_id IS '認証フロー識別用UUID';

-- -----------------------------------------------------
-- 32. OAuth連携情報 (t_user_oauth_connections)
-- 複数OAuthプロバイダー対応
-- -----------------------------------------------------
CREATE TABLE t_user_oauth_connections (
    f_connection_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_provider VARCHAR(50) NOT NULL,           -- 'google', 'apple', 'line'
    f_provider_user_id VARCHAR(255) NOT NULL,  -- Firebase UIDまたはプロバイダーのsub
    f_email_id BIGINT,                         -- t_user_emails への参照
    f_display_name VARCHAR(255),               -- プロバイダーから取得した表示名
    f_avatar_url VARCHAR(500),                 -- プロバイダーから取得したアバター
    f_linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 連携日時
    f_last_used_at TIMESTAMP,                  -- 最後に使用した日時
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 複合ユニーク制約
    UNIQUE (f_provider, f_provider_user_id),   -- 同じプロバイダーユーザーIDは1つのみ
    UNIQUE (f_user_id, f_provider),            -- 1ユーザー1プロバイダーは1連携のみ

    CONSTRAINT fk_oauth_user FOREIGN KEY (f_user_id)
        REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_oauth_email FOREIGN KEY (f_email_id)
        REFERENCES t_user_emails (f_email_id) ON DELETE SET NULL
);

CREATE INDEX idx_oauth_provider ON t_user_oauth_connections (f_provider, f_provider_user_id);
COMMENT ON TABLE t_user_oauth_connections IS 'OAuth連携情報（複数プロバイダー対応）';
CREATE TRIGGER set_timestamp_t_user_oauth_connections BEFORE UPDATE ON t_user_oauth_connections FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();