CREATE TABLE audit_logs
(
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       REFERENCES users (id),
    entity_type VARCHAR(100) NOT NULL,
    entity_id   BIGINT,
    action      VARCHAR(50)  NOT NULL,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user_id    ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity     ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
