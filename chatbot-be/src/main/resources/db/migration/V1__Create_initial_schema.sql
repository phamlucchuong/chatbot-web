
-- Bảng User
CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,          -- UUID lưu dạng chuỗi
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng InvalidatedToken
CREATE TABLE invalidated_tokens (
    id VARCHAR(255) PRIMARY KEY,
    expiry_time DATETIME NOT NULL
);

CREATE TABLE conversations (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);


CREATE TABLE messages (
    id CHAR(36) PRIMARY KEY,
    conversation_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    bot BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE TABLE diseases (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    symptoms TEXT,
    causes TEXT,
    preventions TEXT,
    treatment TEXT
);

-- Thêm chỉ mục cho user_id (để tìm kiếm conversations của một user nhanh hơn)
CREATE INDEX idx_conversations_user_id ON conversations (user_id);

-- Thêm chỉ mục cho conversation_id (để lấy messages trong một conversation nhanh hơn)
CREATE INDEX idx_messages_conversation_id ON messages (conversation_id);