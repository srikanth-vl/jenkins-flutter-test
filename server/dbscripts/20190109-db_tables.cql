CREATE TABLE image_submitted_and_received_data (
    super_app_id uuid,
    app_id uuid,
    project_id uuid,
    image_id uuid,
    submit_status boolean,
    receive_status boolean,
    last_updated_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), project_id, image_id)
);
