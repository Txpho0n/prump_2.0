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

CREATE TABLE IF NOT EXISTS interviews (
                                          id SERIAL PRIMARY KEY,
                                          topic_id BIGINT NOT NULL,
                                          partner1_id TEXT NOT NULL,
                                          partner2_id TEXT NOT NULL,
                                          task_user1 TEXT,
                                          task_user2 TEXT,
                                          room_link TEXT,
                                          start_time TIMESTAMP NOT NULL,
                                          end_time TIMESTAMP NOT NULL
);