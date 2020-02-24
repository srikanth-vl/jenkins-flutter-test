CREATE TABLE dashboard_token_data (
    user_id text,
    token_id uuid,
    token_expired int,
    insert_ts bigint,
    sync_ts bigint,
    PRIMARY KEY ((user_id), token_id)
);

