-- Create table scripts.

CREATE TABLE uniapp_data.superapp_metadata (
    super_app_id uuid,
    name text,
    version int,
    config_file text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id),version)
    ) WITH CLUSTERING ORDER BY (version DESC);

CREATE TABLE uniapp_data.user_meta_data (
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


CREATE TABLE uniapp_data.user_token_data (
    super_app_id uuid,
    user_id text,
    token_id uuid,
    token_expired int,
    insert_ts bigint,
    sync_ts bigint,
    PRIMARY KEY ((super_app_id,user_id), token_id)
);

CREATE TABLE uniapp_data.user_tracking_table (
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


CREATE TABLE uniapp_data.app_meta_data (
    super_app_id uuid,
    app_id uuid,
    parent_app_id uuid,
    config_data text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id),app_id)
);


CREATE TABLE uniapp_data.app_form (
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

CREATE TABLE uniapp_data.form_submission_data (
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

CREATE TABLE uniapp_data.user_project_mapping (
    super_app_id uuid,
    app_id uuid,
    user_id text,
    user_type int,
    project_list list<uuid>,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), user_id, user_type)
);

CREATE TABLE uniapp_data.image_geotag_data (
    super_app_id uuid,
    app_id uuid,
    project_id uuid,
    field_id uuid,
    latitude double,
    longitude double,
    image_data blob,
    sync_ts bigint,
    PRIMARY KEY ((super_app_id, app_id, project_id), field_id)
);


CREATE TABLE uniapp_data.project_master_data (
    super_app_id uuid,
    app_id uuid,
    project_id uuid,
    proj_ext_id text,
    date int,
    key text,
    value text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), project_id , date, key, proj_ext_id)
) WITH CLUSTERING ORDER BY (project_id DESC, date DESC, key DESC);


CREATE TABLE uniapp_data.field_meta_data (
    super_app_id uuid,
    app_id uuid,
    form_type int,
    md_version int,
    md_instance_id text,
    project_id uuid,
    key_type int, //master data, app data, default(for internal system) - > define list of validation rules at the project type level    	
    key text,
    datatype text,
    default_value text,
    uom text,
    target_field text, // master data field
    is_mandatory boolean,
    display_labels text,
    validations text,
    label_name text,
    computation_type text,
    dimension text,
    insert_ts bigint,
    PRIMARY KEY ((super_app_id, app_id), project_id, form_type, md_version, key_type, key)
) WITH CLUSTERING ORDER BY (project_id DESC, form_type DESC, md_version DESC);



CREATE MATERIALIZED VIEW external_to_internal_proj_mapping 
AS SELECT super_app_id, app_id, project_id, proj_ext_id,key,date
FROM project_master_data
WHERE super_app_id IS NOT NULL AND app_id IS NOT NULL AND project_id IS NOT NULL AND proj_ext_id IS NOT NULL  AND key IS NOT NULL  AND date IS NOT NULL
PRIMARY KEY ((super_app_id, app_id), proj_ext_id, project_id, date, key);

//Alter table user_meta_data add mobile_number bigint
//Alter table user_meta_data add is_active boolean
