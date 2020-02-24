DROP TABLE app_meta_data;

CREATE TABLE app_meta_data (
    super_app_id uuid,
    app_id uuid,
    version int,
    parent_app_id uuid,
    config_data text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id),app_id, version)
) WITH CLUSTERING ORDER BY(app_id DESC, version DESC);

ALTER TABLE image_geotag_data ADD media_md_data TEXT;
ALTER TABLE image_geotag_data ADD insert_ts BIGINT;
ALTER TABLE image_geotag_data ADD media_path TEXT;
ALTER TABLE image_geotag_data ADD additional_prop TEXT;



ALTER TABLE superapp_metadata ADD aws_bucket_name TEXT;
UPDATE superapp_metadata SET aws_bucket_name = 'dmeedu' WHERE super_app_id = d0bb80aa-bb86-39b6-a351-13f02e72752b AND version = 1;
UPDATE superapp_metadata SET aws_bucket_name = 'apwrims' WHERE super_app_id = a7cf09ac-43c1-3ba1-aa2d-6a68f786cc44 AND version = 1;
 

