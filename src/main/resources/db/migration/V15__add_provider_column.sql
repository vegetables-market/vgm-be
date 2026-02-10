-- セッションテーブルにプロバイダカラムを追加
ALTER TABLE t_user_sessions ADD COLUMN f_provider VARCHAR(50);
