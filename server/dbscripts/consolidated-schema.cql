CREATE TABLE superapp_metadata (
    super_app_id uuid,
    name text,
    version int,
    config_file text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id),version)
    ) WITH CLUSTERING ORDER BY (version DESC);

CREATE TABLE user_meta_data (
    super_app_id uuid,
    user_id text,
    user_ext_id text,
    user_details text,
    dept_name text,
    otp_object text,
    password text,
    mobile_number bigint,
    app_actions map<uuid, text>, 
    insert_ts bigint,
    is_active boolean,
    PRIMARY KEY ((super_app_id), user_id, user_ext_id));

CREATE MATERIALIZED VIEW external_to_internal_user_mapping 
AS SELECT super_app_id, user_id, user_ext_id
FROM user_meta_data
WHERE super_app_id IS NOT NULL AND user_id IS NOT NULL AND user_ext_id IS NOT NULL 
PRIMARY KEY ((super_app_id), user_ext_id, user_id);

CREATE TABLE user_token_data (
    super_app_id uuid,
    user_id text,
    token_id uuid,
    token_expired int,
    insert_ts bigint,
    sync_ts bigint,
    PRIMARY KEY ((super_app_id,user_id), token_id)
);

CREATE TABLE user_tracking_table (
    super_app_id uuid,
    app_id uuid,
    user_id text,
    token_id uuid,
    timestamp bigint,
    api_type text,
    api text,
    request_success boolean,
    request_object text,
    errors list<text>,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id, user_id), api_type, timestamp, token_id)
    ) WITH CLUSTERING ORDER BY (api_type DESC, timestamp DESC);


CREATE TABLE app_meta_data (
    super_app_id uuid,
    app_id uuid,
    version int,
    parent_app_id uuid,
    config_data text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id),app_id, version)
) WITH CLUSTERING ORDER BY(app_id DESC, version DESC);



CREATE TABLE app_form (
    super_app_id uuid,
    app_id uuid,
    form_type int,
    project_id uuid,
    form_instance_id text,
    form_version_number int,
    md_instance_id text,
    is_active int,
    form_json text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), project_id, form_type, form_version_number)
) WITH CLUSTERING ORDER BY (project_id DESC, form_type DESC, form_version_number DESC);

CREATE TABLE form_submission_data (
    super_app_id uuid,
    form_instance_id text,
    md_instance_id text,
    app_id uuid,
    project_id uuid,
    user_id text,
    user_type int,
    date int,
    timestamp timestamp, // comes from app
    token_id uuid,
    key text,
    datatype text,
    value text,
    db_insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id, project_id), key, date, user_type, timestamp, user_id)
) WITH CLUSTERING ORDER BY (key DESC ,date DESC, user_type ASC, timestamp DESC);

CREATE TABLE image_geotag_data (
    super_app_id uuid,
    app_id uuid,
    project_id uuid,
    field_id uuid,
    latitude double,
    longitude double,
    image_data blob,
    gps_accuracy text,
    media_path text,
    media_type text,
    timestamp_overlay text,
    sync_ts bigint,
    media_md_data text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id, project_id), field_id)
);


CREATE TABLE user_project_mapping (
    super_app_id uuid,
    app_id uuid,
    user_id text,
    user_type int,
    project_id uuid,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), user_id, user_type, project_id)
) WITH CLUSTERING ORDER BY (user_id ASC, user_type ASC, project_id ASC);

CREATE MATERIALIZED VIEW project_to_user_mapping AS
    SELECT super_app_id, app_id, project_id, user_id, user_type
    FROM user_project_mapping
    WHERE super_app_id IS NOT NULL AND app_id IS NOT NULL AND project_id IS NOT NULL AND user_id IS NOT NULL AND user_type IS NOT NULL
    PRIMARY KEY ((super_app_id, app_id), project_id, user_id, user_type)
    WITH CLUSTERING ORDER BY (project_id ASC, user_id ASC, user_type ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';


CREATE TABLE project_master_data (
    super_app_id uuid,
    app_id uuid,
    project_id uuid,
    date int,
    key text,
    insert_ts bigint,
    value text,
    PRIMARY KEY ((super_app_id, app_id), project_id, date, key)
) WITH CLUSTERING ORDER BY (project_id DESC, date DESC, key DESC);


CREATE TABLE field_meta_data (
    super_app_id uuid,
    app_id uuid,
    project_id uuid,
    form_type int,
    md_version int,
    key_type int,
    key text,
    attributes text,
    computation_type text,
    datatype text,
    default_value text,
    dimension text,
    display_labels text,
    insert_ts bigint,
    is_mandatory boolean,
    label_name text,
    md_instance_id text,
    target_field text,
    uom text,
    validations text,
    PRIMARY KEY ((super_app_id, app_id), project_id, form_type, md_version, key_type, key)
) WITH CLUSTERING ORDER BY (project_id DESC, form_type DESC, md_version DESC, key_type ASC, key ASC);

CREATE TABLE entity_meta_data (
    super_app_id uuid,
    app_id uuid,
    project_id uuid,
    user_id text,
    insert_ts bigint,
    parent_entity text,
    entity_name text,
    elements text ,
    PRIMARY KEY ((super_app_id, app_id), project_id, user_id, insert_ts, parent_entity , entity_name)
) WITH CLUSTERING ORDER BY (project_id ASC ,user_id ASC, insert_ts DESC);

CREATE TABLE project_external_to_internal_mapping (
    super_app_id uuid,
    app_id uuid,
    proj_ext_id text,
    project_id uuid,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), proj_ext_id, project_id)
) WITH CLUSTERING ORDER BY (proj_ext_id ASC, project_id ASC);

CREATE MATERIALIZED VIEW project_internal_to_external_mapping_view AS
    SELECT super_app_id, app_id, project_id, proj_ext_id, insert_ts
    FROM project_external_to_internal_mapping
    WHERE super_app_id IS NOT NULL AND app_id IS NOT NULL AND project_id IS NOT NULL AND proj_ext_id IS NOT NULL
    PRIMARY KEY ((super_app_id, app_id), project_id, proj_ext_id)
    WITH CLUSTERING ORDER BY (project_id ASC, proj_ext_id ASC);


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


CREATE TABLE map_files (
    name text,
    size text,
    url text,
    uploaded boolean,
    insert_ts bigint,
    PRIMARY KEY (name));


ALTER TABLE image_submitted_and_received_data ADD relay_status int;
ALTER TABLE image_submitted_and_received_data add submission_ts bigint ;
ALTER TABLE superapp_metadata add package_name text;
ALTER TABLE user_meta_data add map_file_urls map<uuid, text>;
CREATE TABLE map_config_data (
    super_app_id uuid,
    version int,
    config_data text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id),version)
    ) WITH CLUSTERING ORDER BY (version DESC);
CREATE TABLE localization_config_data (
    super_app_id uuid,
    version int,
    config_data text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id),version)
    ) WITH CLUSTERING ORDER BY (version DESC);
CREATE TABLE map_config_data (
    super_app_id uuid,
    app_id uuid,
    version int,
    config_data text,
    markers_info text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id),app_id, version)
) WITH CLUSTERING ORDER BY(app_id DESC, version DESC);
