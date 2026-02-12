-- -----------------------------------------------------
-- 01. ユーザーマスター (m_users)
-- -----------------------------------------------------
CREATE TABLE m_users (
    f_user_id SERIAL PRIMARY KEY,
    f_username VARCHAR(100) NOT NULL UNIQUE,
    f_display_name VARCHAR(100) NOT NULL,
    f_password_hash VARCHAR(255), -- NULL許容（OAuth専用ユーザー対応）
    f_last_login_at TIMESTAMP,
    f_status SMALLINT DEFAULT 1,  -- 0:無効,1:仮登録,2:有効,3:停止,4:削除
    f_role VARCHAR(20) NOT NULL DEFAULT 'USER',
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE m_users IS 'ユーザーマスター';
COMMENT ON COLUMN m_users.f_password_hash IS 'パスワードハッシュ（OAuth専用ユーザーはNULL）';
CREATE TRIGGER set_timestamp_m_users BEFORE UPDATE ON m_users FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 02. ユーザー情報 (t_user_info)
-- -----------------------------------------------------
CREATE TABLE t_user_info (
    f_user_id INTEGER PRIMARY KEY,
    f_last_name VARCHAR(100),
    f_first_name VARCHAR(100),
    f_last_name_kana VARCHAR(100),
    f_first_name_kana VARCHAR(100),
    f_birth_date DATE,
    f_gender SMALLINT DEFAULT 0, -- 0:未選択,1:男性,2:女性,3:その他
    f_phone_number VARCHAR(50),
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_info_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
COMMENT ON TABLE t_user_info IS 'ユーザー詳細情報';
CREATE TRIGGER set_timestamp_t_user_info BEFORE UPDATE ON t_user_info FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 03. ユーザープロフィール (t_user_profile)
-- -----------------------------------------------------
CREATE TABLE t_user_profile (
    f_user_id INTEGER PRIMARY KEY,
    f_profile_text TEXT,
    f_profile_image_url VARCHAR(500),
    f_rating_count INTEGER DEFAULT 0,
    f_rating_sum INTEGER DEFAULT 0,
    f_sales_count INTEGER DEFAULT 0,
    f_purchases_count INTEGER DEFAULT 0,
    f_following_count INTEGER DEFAULT 0,
    f_followers_count INTEGER DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profile_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
COMMENT ON TABLE t_user_profile IS 'ユーザープロフィール';
CREATE TRIGGER set_timestamp_t_user_profile BEFORE UPDATE ON t_user_profile FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 04. ユーザーアドレス (t_user_address)
-- -----------------------------------------------------
CREATE TABLE t_user_address (
    f_address_id SERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_postal_code VARCHAR(100) NOT NULL,
    f_prefecture VARCHAR(50) NOT NULL,
    f_city VARCHAR(100) NOT NULL,
    f_address_line1 VARCHAR(255) NOT NULL,
    f_address_line2 VARCHAR(255),
    f_country_code VARCHAR(2) DEFAULT 'JP',
    f_is_default SMALLINT DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_address_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
COMMENT ON TABLE t_user_address IS 'ユーザー住所情報';
CREATE TRIGGER set_timestamp_t_user_address BEFORE UPDATE ON t_user_address FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 05. ユーザー決済情報 (t_user_pay_info)
-- -----------------------------------------------------
CREATE TABLE t_user_pay_info (
    f_pay_info_id SERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_pay_type SMALLINT NOT NULL, -- 1:Card, 2:PayPay etc
    f_external_customer_id VARCHAR(100), -- Stripe Customer ID
    f_external_payment_method_id VARCHAR(100),
    f_masked_info VARCHAR(50), -- ****-****-****-1234
    f_is_default SMALLINT DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_pay_info_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);
COMMENT ON TABLE t_user_pay_info IS 'ユーザー決済情報';
CREATE TRIGGER set_timestamp_t_user_pay_info BEFORE UPDATE ON t_user_pay_info FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 06. ユーザー名変更履歴 (t_username_history)
-- -----------------------------------------------------
CREATE TABLE t_username_history (
    f_history_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_old_username VARCHAR(100) NOT NULL,
    f_new_username VARCHAR(100) NOT NULL,
    f_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_user_id FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);


-- -----------------------------------------------------
-- 07. ユーザーフォロー情報 (t_user_follows)
-- -----------------------------------------------------
CREATE TABLE t_user_follows (
    f_follow_id BIGSERIAL PRIMARY KEY,
    f_follower_id INTEGER NOT NULL,
    f_followed_id INTEGER NOT NULL,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (f_follower_id, f_followed_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (f_follower_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_followed FOREIGN KEY (f_followed_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- 22. ユーザーメッセージ (t_messages)
-- -----------------------------------------------------
CREATE TABLE t_messages (
    f_message_id BIGSERIAL PRIMARY KEY,
    f_sender_id INTEGER NOT NULL,
    f_receiver_id INTEGER NOT NULL,
    f_item_id BIGINT, -- どの商品に関する問い合わせか(任意)

    f_message_content TEXT,
    f_image_url TEXT, -- 型修正済み
    f_read_status SMALLINT DEFAULT 0, -- 0:未読, 1:既読

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_msg_sender FOREIGN KEY (f_sender_id) REFERENCES m_users (f_user_id),
    CONSTRAINT fk_msg_receiver FOREIGN KEY (f_receiver_id) REFERENCES m_users (f_user_id)
);
CREATE TRIGGER set_timestamp_t_messages BEFORE UPDATE ON t_messages FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();



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
    f_device_name TEXT, -- 例: "Chrome on MacOS"
    f_ip_address VARCHAR(255),   -- IPv6対応
    f_provider VARCHAR(50),     -- 認証プロバイダ

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


-- -----------------------------------------------------
-- 30. ユーザー認証ステータス (t_user_auth_status)
-- 認証関連フラグを集約管理
-- -----------------------------------------------------
CREATE TABLE t_user_auth_status (
    f_user_id INTEGER PRIMARY KEY,

    -- 認証済みフラグ
    f_email_verified BOOLEAN DEFAULT FALSE,
    f_phone_verified BOOLEAN DEFAULT FALSE,
    f_identity_verified BOOLEAN DEFAULT FALSE,  -- 本人確認

    -- MFA設定
    f_is_mfa_enabled BOOLEAN DEFAULT FALSE,
    f_primary_mfa_type VARCHAR(20),  -- 優先MFA方式 (TOTP, SMS, EMAIL)

    -- ログイン試行制限
    f_failed_attempts INT DEFAULT 0,            -- 連続失敗回数
    f_locked_until TIMESTAMP,                   -- ロック終了日時
    f_last_failed_at TIMESTAMP,                 -- 最終失敗日時

    -- 認証方式
    f_has_password BOOLEAN DEFAULT FALSE,       -- パスワード設定済み
    f_last_auth_method VARCHAR(20),             -- 直近のログインで使用した手段 (PASSWORD, GOOGLE等)
    f_last_auth_at TIMESTAMP,                   -- 最後に認証に成功した日時

    -- タイムスタンプ
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auth_status_user FOREIGN KEY (f_user_id)
        REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_auth_status IS 'ユーザー認証ステータス（認証関連フラグを集約）';
COMMENT ON COLUMN t_user_auth_status.f_last_auth_method IS 'PASSWORD, GOOGLE, APPLE, TOTP等';
COMMENT ON COLUMN t_user_auth_status.f_primary_mfa_type IS '自動選択（セキュリティ高い順）';
CREATE TRIGGER set_timestamp_t_user_auth_status BEFORE UPDATE ON t_user_auth_status FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 31. ユーザーメールアドレス (t_user_emails)
-- 複数メールアドレス対応
-- -----------------------------------------------------
CREATE TABLE t_user_emails (
    f_email_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_email VARCHAR(255) NOT NULL,
    f_type VARCHAR(20) NOT NULL,  -- 'PRIMARY', 'OAUTH', 'SUB'
    f_source VARCHAR(50),         -- 'MANUAL', 'GOOGLE', 'APPLE' 等（どこから取得したか）
    f_is_verified BOOLEAN DEFAULT FALSE,
    f_is_primary BOOLEAN DEFAULT FALSE,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 同一メールアドレスは1つのみ（ユーザー横断）
    UNIQUE (f_email),

    CONSTRAINT fk_emails_user FOREIGN KEY (f_user_id)
        REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

CREATE INDEX idx_emails_user ON t_user_emails (f_user_id);
CREATE INDEX idx_emails_primary ON t_user_emails (f_user_id, f_is_primary) WHERE f_is_primary = TRUE;
COMMENT ON TABLE t_user_emails IS 'ユーザーメールアドレス（複数対応）';
COMMENT ON COLUMN t_user_emails.f_type IS 'PRIMARY:メイン, OAUTH:OAuth連携, SUB:サブ';
COMMENT ON COLUMN t_user_emails.f_source IS '取得元（MANUAL, GOOGLE, APPLE等）';
CREATE TRIGGER set_timestamp_t_user_emails BEFORE UPDATE ON t_user_emails FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 34. ユーザーデバイス情報 (t_user_devices)
-- -----------------------------------------------------
CREATE TABLE t_user_devices (
    f_device_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_fcm_token VARCHAR(512) NOT NULL UNIQUE,
    f_device_type VARCHAR(20),
    f_app_version VARCHAR(50),
    f_os_version VARCHAR(50),
    f_last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_devices_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_devices_user_id ON t_user_devices (f_user_id);
COMMENT ON TABLE t_user_devices IS 'ユーザーデバイス情報（Push通知用トークン管理等）';


-- -----------------------------------------------------
-- 35. ゲストセッション (t_guest_sessions)
-- -----------------------------------------------------
CREATE TABLE t_guest_sessions (
    f_guest_id VARCHAR(36) PRIMARY KEY, -- UUID string
    f_expires_at TIMESTAMP NOT NULL,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_guest_sessions IS 'ゲストセッション（一時的なユーザー）';
CREATE TRIGGER set_timestamp_t_guest_sessions BEFORE UPDATE ON t_guest_sessions FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

