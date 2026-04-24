CREATE TABLE interviews
(
    id                   BIGSERIAL    PRIMARY KEY,
    job_application_id   BIGINT       NOT NULL REFERENCES job_applications (id),
    scheduled_at         TIMESTAMP    NOT NULL,
    type                 VARCHAR(50),
    duration_minutes     INTEGER,
    interviewer_name     VARCHAR(255),
    meeting_link         VARCHAR(500),
    notes                TEXT,
    outcome              VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_interviews_scheduled_at        ON interviews (scheduled_at);
CREATE INDEX idx_interviews_job_application_id  ON interviews (job_application_id);
