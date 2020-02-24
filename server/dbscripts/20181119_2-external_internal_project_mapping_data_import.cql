copy project_master_data (super_app_id, app_id, project_id, proj_ext_id, insert_ts) to '/home/ubuntu/apwrims/proj_external_internal_map.csv';

copy project_external_to_internal_mapping (super_app_id, app_id, project_id, proj_ext_id, insert_ts) from '/home/ubuntu/apwrims/proj_external_internal_map.csv';
