CREATE TABLE tags
(
    id      BIGSERIAL    PRIMARY KEY,
    user_id BIGINT       NOT NULL REFERENCES users (id),
    name    VARCHAR(100) NOT NULL,
    color   VARCHAR(7),
    CONSTRAINT uq_tags_user_name UNIQUE (user_id, name)
);

CREATE TABLE job_application_tags
(
    job_application_id BIGINT NOT NULL REFERENCES job_applications (id),
    tag_id             BIGINT NOT NULL REFERENCES tags (id),
    PRIMARY KEY (job_application_id, tag_id)
);
