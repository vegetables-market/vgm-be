CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- テストユーザーを挿入
INSERT INTO users (username, password, email) VALUES
('admin', 'password', 'admin@example.com'),
('testuser', 'test123', 'test@example.com');
