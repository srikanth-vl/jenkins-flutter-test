CREATE TABLE business_analytics_data (
    super_app_id uuid,
    app_id uuid,
    parent_entity text,
    child_entity text,
    computed_values text,
    insert_ts bigint,
    project_ids set<uuid>,
    PRIMARY KEY ((super_app_id,app_id,parent_entity), child_entity)
) WITH CLUSTERING ORDER BY(child_entity DESC);

