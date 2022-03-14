CREATE TABLE IF NOT EXISTS task (
    task_id VARCHAR(36) NOT NULL,
    delay REAL,
    description VARCHAR(100) NOT NULL,
    duration REAL NOT NULL,
    finalized_at TIMESTAMP,
    status VARCHAR(10) NOT NULL,
    PRIMARY KEY (task_id)
);
