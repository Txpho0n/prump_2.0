CREATE TABLE IF NOT EXISTS interviews (
                                          id SERIAL PRIMARY KEY,
                                          partner1_id TEXT NOT NULL,
                                          partner2_id TEXT NOT NULL,
                                          task_user1 TEXT,
                                          task_user2 TEXT,
                                          start_time TIMESTAMP DEFAULT NULL,
                                          end_time TIMESTAMP DEFAULT NULL
);