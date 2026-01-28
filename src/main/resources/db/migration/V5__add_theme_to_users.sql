-- Add a theme column to users table for storing user UI preference (light/dark).
-- If your users table or column uses different naming, adjust accordingly.
ALTER TABLE users ADD COLUMN IF NOT EXISTS theme VARCHAR(16) DEFAULT 'light';
CREATE INDEX IF NOT EXISTS idx_users_theme ON users(theme);
