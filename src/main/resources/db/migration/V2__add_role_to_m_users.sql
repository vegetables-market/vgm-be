-- Add role column to m_users
ALTER TABLE m_users ADD COLUMN f_role VARCHAR(20) DEFAULT 'USER' NOT NULL;
COMMENT ON COLUMN m_users.f_role IS 'ユーザーロール (USER, ADMIN, etc)';
