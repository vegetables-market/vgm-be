-- 「優先するMFAタイプ」のカラムを追加
-- 値の例: 'TOTP', 'EMAIL', 'SMS', NULL (NULLならMFA無効)
ALTER TABLE m_users ADD COLUMN f_preferred_mfa_type VARCHAR(20) DEFAULT NULL;

COMMENT ON COLUMN m_users.f_preferred_mfa_type IS '優先MFA方式 (TOTP, EMAIL, etc). NULLなら無効';