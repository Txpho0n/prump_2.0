CREATE TABLE users (
                       telegram_id VARCHAR(50) PRIMARY KEY,
                       tg_username VARCHAR(255),
                       leetcode_username VARCHAR(255),
                       xp BIGINT DEFAULT 0,
                       league VARCHAR(50),
                       full_name VARCHAR(255),
                       is_admin BOOLEAN DEFAULT FALSE,
                       is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE interviews (
                            id SERIAL PRIMARY KEY,
                            partner1_id VARCHAR(50) REFERENCES users(telegram_id),
                            partner2_id VARCHAR(50) REFERENCES users(telegram_id),
                            task1_slug VARCHAR(255),
                            task2_slug VARCHAR(255),
                            start_time TIMESTAMP,
                            end_time TIMESTAMP
);