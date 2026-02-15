-- -----------------------------------------------------
-- Recovery Sessions (t_recovery_sessions)
-- -----------------------------------------------------
CREATE TABLE t_recovery_sessions (
    f_session_id VARCHAR(36) PRIMARY KEY, -- UUID
    f_user_id INTEGER,
    f_status VARCHAR(20) NOT NULL, -- CREATED, CHALLENGE_SENT, VERIFIED, COMPLETED, LOCKED, EXPIRED
    f_expires_at TIMESTAMP NOT NULL,
    f_attempt_count INTEGER DEFAULT 0,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    f_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recovery_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

CREATE INDEX idx_recovery_user ON t_recovery_sessions (f_user_id);
COMMENT ON TABLE t_recovery_sessions IS 'Account recovery sessions';
CREATE TRIGGER set_timestamp_t_recovery_sessions BEFORE UPDATE ON t_recovery_sessions FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

-- -----------------------------------------------------
-- Password Reset Tokens (t_password_reset_tokens)
-- -----------------------------------------------------
CREATE TABLE t_password_reset_tokens (
    f_token_id BIGSERIAL PRIMARY KEY,
    f_user_id INTEGER NOT NULL,
    f_token_hash VARCHAR(64) NOT NULL, -- SHA-256 hash
    f_expires_at TIMESTAMP NOT NULL,
    f_is_used BOOLEAN DEFAULT FALSE,
    f_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_token_user FOREIGN KEY (f_user_id) REFERENCES m_users (f_user_id) ON DELETE CASCADE
);

CREATE INDEX idx_reset_token_hash ON t_password_reset_tokens (f_token_hash);
CREATE INDEX idx_reset_token_user ON t_password_reset_tokens (f_user_id);
COMMENT ON TABLE t_password_reset_tokens IS 'Password reset tokens';
