-- Java Messenger database schema
-- Run this once in MySQL (e.g. `mysql -u root -p < schema.sql`) before starting the server.

CREATE DATABASE IF NOT EXISTS java_messenger
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE java_messenger;

CREATE TABLE IF NOT EXISTS users (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    email            VARCHAR(100) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    profile_picture  VARCHAR(255) DEFAULT NULL,
    status           ENUM('ONLINE', 'OFFLINE') NOT NULL DEFAULT 'OFFLINE',
    last_seen        DATETIME DEFAULT NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- One row per pair of users who have ever messaged each other.
-- The application always stores user_one_id as the smaller of the two
-- user ids, so a given pair never produces two different conversation rows.
CREATE TABLE IF NOT EXISTS conversations (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_one_id  INT NOT NULL,
    user_two_id  INT NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conv_user_one FOREIGN KEY (user_one_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_user_two FOREIGN KEY (user_two_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_conversation_pair UNIQUE (user_one_id, user_two_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id  INT NOT NULL,
    sender_id        INT NOT NULL,
    receiver_id      INT NOT NULL,
    message          TEXT NOT NULL,
    sent_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read          BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_msg_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id, sent_at);
