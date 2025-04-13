CREATE TABLE users (
                       telegram_id TEXT PRIMARY KEY NOT NULL,
                       tg_username TEXT,
                       leetcode_username TEXT,
                       xp BIGINT NOT NULL DEFAULT 0,
                       league TEXT NOT NULL DEFAULT 'Easy',
                       full_name TEXT NOT NULL,
                       last_mock_interview TIMESTAMP,
                       last_solved_task_timestamp TIMESTAMP,
                       registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       is_admin BOOLEAN NOT NULL DEFAULT FALSE,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE
);



CREATE TABLE interviews (
                            id SERIAL PRIMARY KEY,
                            partner1_id VARCHAR(50) REFERENCES users(telegram_id),
                            partner2_id VARCHAR(50) REFERENCES users(telegram_id),
                            task_user1 VARCHAR(255),
                            task_user2 VARCHAR(255),
                            start_time TIMESTAMP,
                            end_time TIMESTAMP
);