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