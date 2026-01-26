-- t_verification_codesにflow_idを追加
ALTER TABLE t_verification_codes ADD COLUMN f_flow_id VARCHAR(255) UNIQUE;
CREATE INDEX idx_verify_flow_id ON t_verification_codes (f_flow_id);
COMMENT ON COLUMN t_verification_codes.f_flow_id IS '認証フロー識別用UUID';
