CREATE TABLE dashboard_users (
    user_id text,
    password text,
    super_app_ids list<uuid>,
    user_details text,
    hierarchy_access text,
    apps_assigned text, 
    insert_ts bigint,
    is_active boolean,
    PRIMARY KEY (user_id));
