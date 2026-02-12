-- -----------------------------------------------------
-- 27. アプリバージョン管理 (m_app_versions)
-- -----------------------------------------------------
CREATE TABLE m_app_versions (
    f_version_id BIGSERIAL PRIMARY KEY,
    f_platform VARCHAR(20) NOT NULL DEFAULT 'pwa',
    f_version_name VARCHAR(20) NOT NULL, -- '1.2.0'
    f_version_code INTEGER NOT NULL,     -- 120

    -- 最低稼働バージョン (これ未満は強制アップデート対象)
    f_min_supported_version INTEGER NOT NULL DEFAULT 0,

    f_released_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 公開日時
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE m_app_versions IS 'アプリバージョン管理';
CREATE TRIGGER set_timestamp_m_app_versions BEFORE UPDATE ON m_app_versions FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- 28. アプリ更新内容 (m_app_update_notes)
-- -----------------------------------------------------
CREATE TABLE m_app_update_notes (
    f_update_note_id BIGSERIAL PRIMARY KEY,
    f_version_id BIGINT NOT NULL,

    f_title VARCHAR(100) NOT NULL,
    f_message TEXT NOT NULL,

    -- 重要度 (UI表示用: 'HIGH', 'NORMAL' など)
    f_importance VARCHAR(20) DEFAULT 'NORMAL',

    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notes_version FOREIGN KEY (f_version_id) REFERENCES m_app_versions (f_version_id) ON DELETE CASCADE
);
COMMENT ON TABLE m_app_update_notes IS 'アプリ更新内容詳細';
CREATE TRIGGER set_timestamp_m_app_update_notes BEFORE UPDATE ON m_app_update_notes FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();
