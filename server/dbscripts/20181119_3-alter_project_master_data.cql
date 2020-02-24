COPY uniapp_data.project_master_data (super_app_id, app_id, project_id, date, key, value, insert_ts) to '/home/ubuntu/apwrims/project_master_data.csv';

DROP MATERIALIZED VIEW external_to_internal_proj_mapping;
DROP table project_master_data;

CREATE TABLE uniapp_data.project_master_data (
    super_app_id uuid,
    app_id uuid,
    project_id uuid,
    date int,
    key text,
    value text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), project_id , date, key)
) WITH CLUSTERING ORDER BY (project_id DESC, date DESC, key DESC);

COPY uniapp_data.project_master_data (super_app_id, app_id, project_id, date, key, value, insert_ts) from '/home/ubuntu/apwrims/project_master_data.csv';
