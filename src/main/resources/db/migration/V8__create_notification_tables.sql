-- -----------------------------------------------------
-- 33. 通知設定 (t_notification_settings)
-- -----------------------------------------------------
CREATE TABLE t_notification_settings (
    f_user_id INT PRIMARY KEY REFERENCES m_users(f_user_id) ON DELETE CASCADE,
    f_email_notifications BOOLEAN DEFAULT TRUE,
    f_favorite_price_drop BOOLEAN DEFAULT TRUE,
    f_new_message BOOLEAN DEFAULT TRUE,
    f_transaction_updates BOOLEAN DEFAULT TRUE,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- トリガー設定
CREATE TRIGGER set_timestamp_t_notification_settings
    BEFORE UPDATE ON t_notification_settings
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

-- コメント
COMMENT ON TABLE t_notification_settings IS '通知設定';
COMMENT ON COLUMN t_notification_settings.f_user_id IS 'ユーザーID';
COMMENT ON COLUMN t_notification_settings.f_email_notifications IS 'メール通知設定フラグ';
COMMENT ON COLUMN t_notification_settings.f_favorite_price_drop IS 'お気に入り価格低下通知設定';
COMMENT ON COLUMN t_notification_settings.f_new_message IS 'メッセージ受信通知設定';
COMMENT ON COLUMN t_notification_settings.f_transaction_updates IS '取引更新通知設定';
COMMENT ON COLUMN t_notification_settings.f_created_at IS '作成日時';
COMMENT ON COLUMN t_notification_settings.f_updated_at IS '更新日時';

