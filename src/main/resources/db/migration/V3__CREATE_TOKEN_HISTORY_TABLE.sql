CREATE TABLE token_history (
    id BIGSERIAL PRIMARY KEY,
    user_token_id BIGINT REFERENCES user_token (id),
    access_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX token_history_access_time ON token_history(access_at);