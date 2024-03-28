CREATE TABLE IF NOT EXISTS book (
    id UUID NOT NULL,
    study_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    state TEXT,
    PRIMARY KEY (id)
);
