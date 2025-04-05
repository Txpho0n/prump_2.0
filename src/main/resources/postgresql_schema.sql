CREATE TABLE users (
                       telegram_id TEXT PRIMARY KEY NOT NULL,
                       tg_username TEXT,
                       xp BIGINT NOT NULL DEFAULT 0,
                       full_name TEXT NOT NULL,
                       last_mock_interview TIMESTAMP,
                       last_solved_task_timestamp TIMESTAMP,
                       registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       is_admin BOOLEAN NOT NULL DEFAULT FALSE
);


