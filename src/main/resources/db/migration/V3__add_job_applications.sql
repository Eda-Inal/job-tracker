CREATE TABLE job_applications
(
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES users (id),
    company_name     VARCHAR(255) NOT NULL,
    position         VARCHAR(255) NOT NULL,
    status           VARCHAR(50)  NOT NULL DEFAULT 'APPLIED',
    application_date DATE         NOT NULL,
    job_url          VARCHAR(500),
    location         VARCHAR(255),
    work_type        VARCHAR(50),
    salary_min       INTEGER,
    salary_max       INTEGER,
    currency         VARCHAR(10)  NOT NULL DEFAULT 'TRY',
    notes            TEXT,
    source           VARCHAR(50),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at       TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_job_applications_user_deleted  ON job_applications (user_id, deleted);
CREATE INDEX idx_job_applications_user_status   ON job_applications (user_id, status);
CREATE INDEX idx_job_applications_user_date     ON job_applications (user_id, application_date);
