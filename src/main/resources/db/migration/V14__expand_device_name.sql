/*
  User AgentやIPv6アドレスが長いため、カラム長を拡張します。
*/

-- デバイス名をTEXT型に変更（User-Agent全体を格納するため）
ALTER TABLE t_user_sessions ALTER COLUMN f_device_name TYPE TEXT;

-- IPアドレスを255文字に拡張（念のため）
ALTER TABLE t_user_sessions ALTER COLUMN f_ip_address TYPE VARCHAR(255);
