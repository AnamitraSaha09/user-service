CREATE TABLE user_profile (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    display_name  VARCHAR(100) NOT NULL,
    timezone      VARCHAR(50)  NOT NULL DEFAULT 'UTC',
    notify_email  BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_sms    BOOLEAN      NOT NULL DEFAULT FALSE,
    phone         VARCHAR(30),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version       BIGINT       NOT NULL DEFAULT 0
);
