ALTER TABLE users
    ADD COLUMN social_rating FLOAT DEFAULT 0.0;

CREATE TABLE ratings (
                         id SERIAL PRIMARY KEY,
                         rater_id TEXT NOT NULL REFERENCES users(telegram_id),
                         rated_id TEXT NOT NULL REFERENCES users(telegram_id),
                         interview_id BIGINT NOT NULL REFERENCES interviews(id),
                         rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 10),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         UNIQUE(rater_id, rated_id, interview_id)
);