-- V7: 通知設定テーブル作成

-- 通知設定テーブル
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
COMMENT ON TABLE t_notification_settings IS 'ユーザー通知設定';
COMMENT ON COLUMN t_notification_settings.f_user_id IS 'ユーザーID';
COMMENT ON COLUMN t_notification_settings.f_email_notifications IS 'メール通知の全体ON/OFF';
COMMENT ON COLUMN t_notification_settings.f_favorite_price_drop IS 'お気に入り在庫値下げ通知';
COMMENT ON COLUMN t_notification_settings.f_new_message IS '新着メッセージ通知';
COMMENT ON COLUMN t_notification_settings.f_transaction_updates IS '取引関連更新通知';
