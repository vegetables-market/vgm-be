-- m_usersテーブルにメールアドレスカラムを追加
ALTER TABLE m_users ADD COLUMN f_email VARCHAR(255) UNIQUE;
COMMENT ON COLUMN m_users.f_email IS 'メールアドレス';
