
CREATE TABLE uniapp_data.project_external_to_internal_mapping (
    super_app_id uuid,
    app_id uuid,
    proj_ext_id text,
    project_id uuid,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), proj_ext_id, project_id)
);

CREATE MATERIALIZED VIEW uniapp_data.project_internal_to_external_mapping_view
AS SELECT super_app_id, app_id, project_id, proj_ext_id, insert_ts
FROM project_external_to_internal_mapping
WHERE super_app_id IS NOT NULL AND app_id IS NOT NULL AND project_id IS NOT NULL AND proj_ext_id IS NOT NULL
PRIMARY KEY ((super_app_id, app_id), project_id, proj_ext_id);
