CREATE TABLE config (
    key VARCHAR(50) PRIMARY KEY,
    value TEXT NOT NULL
);

INSERT INTO config (key, value) VALUES ('topic', 'default_topic');
