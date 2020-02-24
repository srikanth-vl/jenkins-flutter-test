CREATE TABLE project_submission_analytics (
    super_app_id uuid,
    app_id uuid,
    parent_entity text,
    date int,
    entity text,
    no_of_successful_submissions int,
    no_of_failed_submissions int,
    successful_submission_project_ids set<uuid>,
    failed_submission_project_ids set<uuid>,
    user_ids set<text>,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id,app_id,parent_entity), date, entity)
) WITH CLUSTERING ORDER BY(date DESC, entity DESC);

