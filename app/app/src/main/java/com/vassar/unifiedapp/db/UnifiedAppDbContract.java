package com.vassar.unifiedapp.db;

import android.provider.BaseColumns;

public class UnifiedAppDbContract {

    private UnifiedAppDbContract() {}

    // Inner static classes defining each table
    public static class ConfigFilesEntry implements BaseColumns {
        public static final String TABLE_CONFIG = "configfilestable";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_CONFIG_NAME = "config_name";
        public static final String COLUMN_CONFIG_FILE_CONTENT = "config_content";
        public static final String COLUMN_CONFIG_VERSION = "config_version";
        public static final String COLUMN_CONFIG_LAST_SYNC_TS = "config_last_sync_ts";
    }

    public static class UserMetaEntry implements BaseColumns {
        public static final String TABLE_USER = "usermetatable";
        public static final String COLUMN_USER_ID = "userid";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_TOKEN = "token";
        public static final String COLUMN_LAST_NETWORK_SYNC_TIME = "lastnetworksync";
        public static final String COLUMN_IS_LOGGEN_IN = "isloggedin";
        public static final String COLUMN_LAST_LOGIN_TS = "lastlogints";
        public static final String COLUMN_USER_DETAILS = "userdetails";
    }

    public static class IncomingImagesEntry implements BaseColumns {
        public static final String TABLE_IMAGES = "incomingimagestable";
        public static final String COLUMN_IMAGE_TYPE = "imagetype";
        public static final String COLUMN_IMAGE_URL = "imageurl";
        public static final String COLUMN_IMAGE_LOCAL_PATH = "imagelocalpath";
    }

    public static class FormMediaEntry implements BaseColumns {
        public static final String TABLE_FORM_MEDIA = "form_media_table";
        public static final String COLUMN_FORM_MEDIA_APP_ID = "media_appid";
        public static final String COLUMN_FORM_MEDIA_USER_ID = "media_userid";
        public static final String COLUMN_FORM_MEDIA_UUID = "media_uuid";
        public static final String COLUMN_FORM_SUBMISSION_TIMESTAMP = "form_submission_timestamp";
        public static final String COLUMN_FORM_MEDIA_PROJECT_ID = "media_projectid";
        public static final String COLUMN_FORM_MEDIA_LOCAL_PATH = "media_local_path";
        public static final String COLUMN_FORM_MEDIA_BITMAP = "media_bitmap";
        public static final String COLUMN_FORM_MEDIA_HAS_GEOTAG = "media_hasgeotag";
        public static final String COLUMN_FORM_MEDIA_LATITUDE = "media_latitude";
        public static final String COLUMN_FORM_MEDIA_LONGITUDE = "media_longitude";
        public static final String COLUMN_FORM_MEDIA_GPS_ACCURACY = "media_gps_accuracy";
        public static final String COLUMN_FORM_MEDIA_TYPE = "media_type"; //image/video/pdf
        public static final String COLUMN_FORM_MEDIA_SUBTYPE = "media_subtype"; //thumbnail, preview, full
        public static final String COLUMN_FORM_MEDIA_EXTENSION = "media_extension"; //.JPEG, .JPG, .PNG,.MP4
        public static final String COLUMN_FORM_MEDIA_CLICK_TS = "media_click_ts";  // time at which media is taken
        public static final String COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP = "media_upload_ts"; // timestamp from server after successful upload
        public static final String COLUMN_FORM_MEDIA_ACTION_TYPE = "media_action_type"; // upload or download
        public static final String COLUMN_FORM_MEDIA_UPLOAD_RETRIES = "media_upload_retries"; // trials for upload
        public static final String COLUMN_FORM_MEDIA_REQUEST_STATUS = "media_request_status"; // status of request upload : NEW = 0, SUCCESS = 1, FAILURE = 2, RETRY = 3;
        public static final String COLUMN_ADDITIONAL_PROPERTIES = "additional_props";   //like project external id
    }

    public static class ProjectTableEntry implements BaseColumns {
        public static final String TABLE_PROJECT = "projecttable";
        public static final String COLUMN_PROJECT_APP_ID = "projectappid";
        public static final String COLUMN_PROJECT_USER_ID = "projectuserid";
        public static final String COLUMN_PROJECT_ID = "projectid";
        public static final String COLUMN_PROJECT_NAME = "projectname";
        public static final String COLUMN_PROJECT_LAT = "projectlat";
        public static final String COLUMN_PROJECT_LON = "projectlon";
        public static final String COLUMN_PROJECT_BBOX = "projectbbox";
        public static final String COLUMN_PROJECT_CIRCLE_VALIDATION = "projectcircle";
        public static final String COLUMN_PROJECT_LAST_SUB_DATE = "projectlastsubdate";
        public static final String COLUMN_PROJECT_STATE = "projectstate";
        public static final String COLUMN_PROJECT_EXTERNAL_PROJECT_ID = "projectextprojid";
        public static final String COLUMN_PROJECT_FIELDS = "projectfields";
        public static final String COLUMN_PROJECT_USER_TYPE = "projectusertype";
        public static final String COLUMN_PROJECT_VALIDATIONS = "projectvalidations";
        public static final String COLUMN_PROJECT_ICON = "projecticon";
        public static final String COLUMN_SERVER_SYNC_TS = "projectserversyncts";
        public static final String COLUMN_SHOW_MAP = "projectshowmap";
        public static final String COLUMN_FILTERING_DIMENSION_VALUES = "projectfilteringdimensionnames";
        public static final String COLUMN_GROUPING_DIMENSION_VALUES = "projectgroupingdimensionnames";
        public static final String COLUMN_PROJECT_LAST_UPDATED_TS = "projectlastupdatedts";
        public static final String COLUMN_PROJECT_ASSINGED_STATUS = "projectassignedstatus";
    }

    public static class ProjectSubmissionEntry implements BaseColumns {
        public static final String TABLE_PROJECT_SUBMISSION = "projectsubmissiontable";
        public static final String COLUMN_PROJECT_SUBMISSION_APP_ID = "appid";
        public static final String COLUMN_PROJECT_SUBMISSION_USER_ID = "userid";
        public static final String COLUMN_PROJECT_SUBMISSION_USER_TYPE = "usertype";
        public static final String COLUMN_PROJECT_SUBMISSION_FORM_ID = "formid";
        public static final String COLUMN_PROJECT_SUBMISSION_TIMESTAMP = "timestamp";
        public static final String COLUMN_PROJECT_SUBMISSION_PROJECT_ID = "projectid";
        public static final String COLUMN_PROJECT_SUBMISSION_FIELDS = "submissionobject";
        public static final String COLUMN_PROJECT_SUBMISSION_API = "submissionapi";
        public static final String COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID = "mdinstanceid";
        public static final String COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS = "submissionstatus";
        public static final String COLUMN_PROJECT_SUBMISSION_RESPONSE = "response";
        public static final String COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS= "serversyncts";
        public static final String COLUMN_PROJECT_SUBMISSION_RETRY_COUNT= "uploadretrycount";
        public static final String COLUMN_ADDITIONAL_PROPERTIES = "additionalproperties";

    }
    public static class ProjectFormTableEntry implements BaseColumns {
        public static final String TABLE_PROJECT_FORM = "project_form_table";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_APP_ID = "app_id";
        public static final String COLUMN_PROJECT_ID = "project_id";
        public static final String COLUMN_FORM_TYPE= "form_type";
        public static final String COLUMN_FORM_DATA = "form_data";
        public static final String COLUMN_MD_INSTANCE_ID = "md_instance_id";
        public static final String COLUMN_VERSION = "form_version";
    }
    public static class EntityMetaEntry implements BaseColumns {

        public static final String TABLE_ENTITY_METADATA = "entity_metadata";
        public static final String COLUMN_SUPER_APP_ID = "super_app_id";
        public static final String COLUMN_APP_ID = "app_id";
        public static final String COLUMN_PROJECT_ID = "project_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_PARENT_ENTITY = "parent_entity";
        public static final String COLUMN_ENTITY_NAME = "entity_name";
        public static final String COLUMN_ELEMENTS = "elements";
        public static final String COLUMN_INSERT_TIMESTAMP = "insert_timestamp";

    }
}
