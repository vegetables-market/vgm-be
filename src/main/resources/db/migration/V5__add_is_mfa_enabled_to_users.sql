-- m_usersテーブルにf_is_mfa_enabledカラムを追加
ALTER TABLE m_users ADD COLUMN f_is_mfa_enabled BOOLEAN DEFAULT FALSE NOT NULL;

-- 既存のMFA設定に基づいてフラグを更新（t_user_two_factorテーブルが存在する場合）
UPDATE m_users
SET f_is_mfa_enabled = TRUE
WHERE f_user_id IN (
    SELECT f_user_id FROM t_user_two_factor WHERE f_is_enabled = TRUE
);
