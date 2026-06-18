CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    middle_name   VARCHAR(100),
    avatar_key    VARCHAR(512),
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL
);

CREATE TABLE tasks (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    task_date           DATE NOT NULL,
    start_time          TIME NOT NULL,
    interval_start      TIME,
    interval_end        TIME,
    color               VARCHAR(20) NOT NULL,
    notification_offset VARCHAR(20),
    is_completed        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);
CREATE INDEX idx_tasks_user_date ON tasks (user_id, task_date);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    revoked_at  TIMESTAMP,
    replaced_by UUID REFERENCES refresh_tokens (id) ON DELETE SET NULL
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE device_sessions (
    id            UUID PRIMARY KEY,
    user_id       UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    device_uuid   VARCHAR(128) NOT NULL,
    os            VARCHAR(16) NOT NULL,
    os_version    VARCHAR(32) NOT NULL,
    app_version   VARCHAR(32) NOT NULL,
    device_model  VARCHAR(128),
    created_at    TIMESTAMP NOT NULL,
    last_seen_at  TIMESTAMP NOT NULL,
    CONSTRAINT uq_device_session UNIQUE (user_id, device_uuid)
);
