class BaseColumns {}

class ConfigFilesEntry extends BaseColumns {
  static const String TABLE_CONFIG = "config_file_table";
  static const String COLUMN_USER_ID = "user_id";
  static const String COLUMN_CONFIG_NAME = "config_name";
  static const String COLUMN_CONFIG_FILE_CONTENT = "config_content";
  static const String COLUMN_CONFIG_VERSION = "config_version";
  static const String COLUMN_CONFIG_LAST_SYNC_TS = "config_last_sync_ts";

  static const String CREATE_TABLE_QUERY = 'CREATE TABLE ' +
      TABLE_CONFIG +
      '(' +
      COLUMN_CONFIG_NAME +
      " TEXT," +
      COLUMN_USER_ID +
      " TEXT," +
      COLUMN_CONFIG_VERSION +
      " INTEGER," +
      COLUMN_CONFIG_FILE_CONTENT +
      " TEXT," +
      COLUMN_CONFIG_LAST_SYNC_TS +
      " INTEGER," +
      " PRIMARY KEY (" +
      COLUMN_USER_ID +
      ", " +
      COLUMN_CONFIG_NAME +
      ")" +
      ")";
  static const String DELETE_CONFIG_FILE_TABLE =
      "DROP TABLE IF EXISTS " + TABLE_CONFIG;

  static String PRIMARY_KEY_WHERE_STRING =
      COLUMN_USER_ID + " = ? AND " + COLUMN_CONFIG_NAME + " = ?";
}

class UserMetaEntry implements BaseColumns {
  static const String TABLE_USER = "user_metadata_table";
  static const String COLUMN_USER_ID = "userid";
  static const String COLUMN_PASSWORD = "password";
  static const String COLUMN_TOKEN = "token";
  static const String COLUMN_LAST_NETWORK_SYNC_TIME = "last_networks_ync";
  static const String COLUMN_IS_LOGGED_IN = "is_loggedin";
  static const String COLUMN_LAST_LOGIN_TS = "last_login_ts";
  static const String COLUMN_USER_DETAILS = "user_details";

  static const String CREATE_USER_META_TABLE = "CREATE TABLE " +
      TABLE_USER +
      " (" +
      COLUMN_USER_ID +
      " TEXT," +
      COLUMN_PASSWORD +
      " TEXT," +
      COLUMN_TOKEN +
      " TEXT," +
      COLUMN_LAST_NETWORK_SYNC_TIME +
      " INTEGER," +
      COLUMN_LAST_LOGIN_TS +
      " INTEGER," +
      COLUMN_IS_LOGGED_IN +
      " INTEGER," +
      COLUMN_USER_DETAILS +
      " TEXT," +
      " PRIMARY KEY (" +
      COLUMN_USER_ID +
      ")" +
      ")";
  static const String DELETE_USER_META_TABLE =
      "DROP TABLE IF EXISTS " + TABLE_USER;

  static String whereClause = COLUMN_USER_ID + " = ?";
}

class IncomingImagesEntry implements BaseColumns {
  static const String TABLE_IMAGES = "incoming_images_table";
  static const String COLUMN_IMAGE_TYPE = "image_stype";
  static const String COLUMN_IMAGE_URL = "image_url";
  static const String COLUMN_IMAGE_LOCAL_PATH = "image_local_path";
  static const String COLUMN_IMAGE_NAME = "image_name";
  static const String COLUMN_DOWNLOAD_STATUS = "download_status";
}

class FormMediaEntry implements BaseColumns {
  static const String TABLE_FORM_MEDIA = "form_media_table";
  static const String COLUMN_FORM_MEDIA_APP_ID = "media_app_id";
  static const String COLUMN_FORM_MEDIA_USER_ID = "media_user_id";
  static const String COLUMN_FORM_MEDIA_UUID = "media_uuid";
  static const String COLUMN_FORM_SUBMISSION_TIMESTAMP =
      "form_submission_timestamp";
  static const String COLUMN_FORM_MEDIA_PROJECT_ID = "media_project_id";
  static const String COLUMN_FORM_MEDIA_LOCAL_PATH = "media_local_path";
  static const String COLUMN_FORM_MEDIA_BITMAP = "media_bitmap";
  static const String COLUMN_FORM_MEDIA_HAS_GEOTAG = "media_hasgeotag";
  static const String COLUMN_FORM_MEDIA_LATITUDE = "media_latitude";
  static const String COLUMN_FORM_MEDIA_LONGITUDE = "media_longitude";
  static const String COLUMN_FORM_MEDIA_GPS_ACCURACY = "media_gps_accuracy";
  static const String COLUMN_FORM_MEDIA_TYPE = "media_type"; //image/video/pdf
  static const String COLUMN_FORM_MEDIA_SUBTYPE =
      "media_subtype"; //thumbnail, preview, full
  static const String COLUMN_FORM_MEDIA_EXTENSION =
      "media_extension"; //.JPEG, .JPG, .PNG,.MP4
  static const String COLUMN_FORM_MEDIA_CLICK_TS =
      "media_click_ts"; // time at which media is taken
  static const String COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP =
      "media_upload_ts"; // timestamp from server after successful upload
  static const String COLUMN_FORM_MEDIA_ACTION_TYPE =
      "media_action_type"; // upload or download
  static const String COLUMN_FORM_MEDIA_UPLOAD_RETRIES =
      "media_upload_retries"; // trials for upload
  static const String COLUMN_FORM_MEDIA_REQUEST_STATUS =
      "media_request_status"; // status of request upload : NEW = 0, SUCCESS = 1, FAILURE = 2, RETRY = 3;
  static const String COLUMN_ADDITIONAL_PROPERTIES =
      "additional_props"; //like project external id

//Form Media Table
  static final String SQL_CREATE_FORM_MEDIA_TABLE = "CREATE TABLE " +
      TABLE_FORM_MEDIA +
      " (" +
      COLUMN_FORM_MEDIA_APP_ID +
      " TEXT," +
      COLUMN_FORM_MEDIA_USER_ID +
      " TEXT," +
      COLUMN_FORM_MEDIA_UUID +
      " TEXT," +
      COLUMN_FORM_SUBMISSION_TIMESTAMP +
      " INTEGER," +
      COLUMN_FORM_MEDIA_PROJECT_ID +
      " TEXT," +
      COLUMN_FORM_MEDIA_LOCAL_PATH +
      " TEXT," +
      COLUMN_FORM_MEDIA_BITMAP +
      " BLOB," +
      COLUMN_FORM_MEDIA_HAS_GEOTAG +
      " INTEGER," +
      COLUMN_FORM_MEDIA_LATITUDE +
      " TEXT," +
      COLUMN_FORM_MEDIA_LONGITUDE +
      " TEXT," +
      COLUMN_FORM_MEDIA_GPS_ACCURACY +
      " TEXT," +
      COLUMN_FORM_MEDIA_TYPE +
      " INTEGER," +
      COLUMN_FORM_MEDIA_REQUEST_STATUS +
      " INTEGER," +
      COLUMN_FORM_MEDIA_SUBTYPE +
      " INTEGER," +
      COLUMN_FORM_MEDIA_EXTENSION +
      " TEXT," +
      COLUMN_FORM_MEDIA_CLICK_TS +
      " INTEGER," +
      COLUMN_FORM_MEDIA_UPLOAD_RETRIES +
      " INTEGER," +
      COLUMN_FORM_MEDIA_ACTION_TYPE +
      " INTEGER, " +
      COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP +
      " INTEGER," +
      COLUMN_ADDITIONAL_PROPERTIES +
      " TEXT," +
      " PRIMARY KEY (" +
      COLUMN_FORM_MEDIA_APP_ID +
      "," +
      COLUMN_FORM_MEDIA_USER_ID +
      "," +
      COLUMN_FORM_MEDIA_UUID +
      "))";

  static final String SQL_DELETE_MEDIA_IMAGES_TABLE =
      "DROP TABLE IF EXISTS " + TABLE_FORM_MEDIA;

  static final String PRIMARY_KEY_WHERE_STRING = "${COLUMN_FORM_MEDIA_APP_ID} = ? AND ${COLUMN_FORM_MEDIA_USER_ID } = ? AND ${COLUMN_FORM_MEDIA_UUID} = ?";
}

class ProjectTableEntry implements BaseColumns {
  static const String TABLE_PROJECT = "projecttable";
  static const String COLUMN_PROJECT_APP_ID = "projectappid";
  static const String COLUMN_PROJECT_USER_ID = "projectuserid";
  static const String COLUMN_PROJECT_ID = "projectid";
  static const String COLUMN_PROJECT_NAME = "projectname";
  static const String COLUMN_PROJECT_LAT = "projectlat";
  static const String COLUMN_PROJECT_LON = "projectlon";
  static const String COLUMN_PROJECT_BBOX = "projectbbox";
  static const String COLUMN_PROJECT_CIRCLE_VALIDATION = "projectcircle";
  static const String COLUMN_PROJECT_LAST_SUB_DATE = "projectlastsubdate";
  static const String COLUMN_PROJECT_STATE = "projectstate";
  static const String COLUMN_PROJECT_EXTERNAL_PROJECT_ID = "projectextprojid";
  static const String COLUMN_PROJECT_FIELDS = "projectfields";
  static const String COLUMN_PROJECT_USER_TYPE = "projectusertype";
  static const String COLUMN_PROJECT_VALIDATIONS = "projectvalidations";
  static const String COLUMN_PROJECT_ICON = "projecticon";
  static const String COLUMN_SERVER_SYNC_TS = "projectserversyncts";
  static const String COLUMN_SHOW_MAP = "projectshowmap";
  static const String COLUMN_FILTERING_DIMENSION_VALUES =
      "projectfilteringdimensionnames";
  static const String COLUMN_GROUPING_DIMENSION_VALUES =
      "projectgroupingdimensionnames";
  static const String COLUMN_PROJECT_LAST_UPDATED_TS = "projectlastupdatedts";
  static const String COLUMN_PROJECT_ASSINGED_STATUS = "projectassignedstatus";

  static final String SQL_CREATE_PROJECT_TABLE = "CREATE TABLE " +
      TABLE_PROJECT +
      " (" +
      COLUMN_PROJECT_APP_ID +
      " TEXT," +
      COLUMN_PROJECT_USER_ID +
      " TEXT," +
      COLUMN_PROJECT_ID +
      " TEXT," +
      COLUMN_PROJECT_NAME +
      " TEXT," +
      COLUMN_PROJECT_LAT +
      " TEXT," +
      COLUMN_PROJECT_LON +
      " TEXT," +
      COLUMN_PROJECT_BBOX +
      " TEXT," +
      COLUMN_PROJECT_CIRCLE_VALIDATION +
      " TEXT," +
      COLUMN_PROJECT_LAST_SUB_DATE +
      " TEXT," +
      COLUMN_PROJECT_STATE +
      " TEXT," +
      COLUMN_PROJECT_EXTERNAL_PROJECT_ID +
      " TEXT," +
      COLUMN_PROJECT_FIELDS +
      " TEXT," +
      COLUMN_PROJECT_USER_TYPE +
      " TEXT," +
      COLUMN_PROJECT_VALIDATIONS +
      " TEXT," +
      COLUMN_SERVER_SYNC_TS +
      " LONG," +
      COLUMN_SHOW_MAP +
      " BOOLEAN," +
      COLUMN_PROJECT_ASSINGED_STATUS +
      " BOOLEAN," +
      COLUMN_FILTERING_DIMENSION_VALUES +
      " TEXT," +
      COLUMN_GROUPING_DIMENSION_VALUES +
      " TEXT," +
      COLUMN_PROJECT_LAST_UPDATED_TS +
      " LONG," +
      COLUMN_PROJECT_ICON +
      " TEXT," +
      " PRIMARY KEY (" +
      COLUMN_PROJECT_APP_ID +
      ", " +
      COLUMN_PROJECT_USER_ID +
      "," +
      COLUMN_PROJECT_ID +
      ")" +
      ")";

  // TODO : Pick the latest submission among both the tables
  static final String SQL_DELETE_PROJECT_TABLE =
      "DROP TABLE IF EXISTS " + TABLE_PROJECT;

  static final String PRIMARY_KEY_WHERE_STRING =
      COLUMN_PROJECT_APP_ID + " = ? AND " +
      COLUMN_PROJECT_USER_ID + " = ? AND " +
      COLUMN_PROJECT_ID + " = ?";
}

class ProjectSubmissionEntry implements BaseColumns {
  static const String TABLE_PROJECT_SUBMISSION = "projectsubmissiontable";
  static const String COLUMN_PROJECT_SUBMISSION_APP_ID = "appid";
  static const String COLUMN_PROJECT_SUBMISSION_USER_ID = "userid";
  static const String COLUMN_PROJECT_SUBMISSION_USER_TYPE = "usertype";
  static const String COLUMN_PROJECT_SUBMISSION_FORM_ID = "formid";
  static const String COLUMN_PROJECT_SUBMISSION_TIMESTAMP = "timestamp";
  static const String COLUMN_PROJECT_SUBMISSION_PROJECT_ID = "projectid";
  static const String COLUMN_PROJECT_SUBMISSION_FIELDS = "submissionobject";
  static const String COLUMN_PROJECT_SUBMISSION_API = "submissionapi";
  static const String COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID = "mdinstanceid";
  static const String COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS =
      "submissionstatus";
  static const String COLUMN_PROJECT_SUBMISSION_RESPONSE = "response";
  static const String COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS = "serversyncts";
  static const String COLUMN_PROJECT_SUBMISSION_RETRY_COUNT =
      "uploadretrycount";
  static const String COLUMN_ADDITIONAL_PROPERTIES = "additionalproperties";

  //Form Submittion table
  static final String SQL_CREATE_PROJECT_SUBMISSION_TABLE = "CREATE TABLE " +
      TABLE_PROJECT_SUBMISSION +
      " (" +
      COLUMN_PROJECT_SUBMISSION_APP_ID +
      " TEXT," +
      COLUMN_PROJECT_SUBMISSION_USER_ID +
      " TEXT," +
      COLUMN_PROJECT_SUBMISSION_USER_TYPE +
      " TEXT," +
      COLUMN_PROJECT_SUBMISSION_PROJECT_ID +
      " TEXT," +
      COLUMN_PROJECT_SUBMISSION_FORM_ID +
      " TEXT," +
      COLUMN_PROJECT_SUBMISSION_TIMESTAMP +
      " LONG," +
      COLUMN_PROJECT_SUBMISSION_FIELDS +
      " TEXT," +
      COLUMN_PROJECT_SUBMISSION_API +
      " TEXT," +
      COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID +
      " TEXT," +
      // TODO : The Upload Error - 1) Validation Error 2) Upload Error
      COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS +
      " INTEGER," +
      COLUMN_PROJECT_SUBMISSION_RESPONSE +
      " TEXT," +
      COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS +
      " LONG," +
      COLUMN_PROJECT_SUBMISSION_RETRY_COUNT +
      " INTEGER," +
      COLUMN_ADDITIONAL_PROPERTIES +
      " TEXT," +
      " PRIMARY KEY (" +
      COLUMN_PROJECT_SUBMISSION_APP_ID +
      ", " +
      COLUMN_PROJECT_SUBMISSION_USER_ID +
      "," +
      COLUMN_PROJECT_SUBMISSION_PROJECT_ID +
      "," +
      COLUMN_PROJECT_SUBMISSION_TIMESTAMP +
      ")" +
      ")";

  static final String SQL_DELETE_PROJECT_SUBMISSION_TABLE =
      "DROP TABLE IF EXISTS " + TABLE_PROJECT_SUBMISSION;

  static final String PRIMARY_KEY_WHERE_STRING =
  '${COLUMN_PROJECT_SUBMISSION_APP_ID} = ? AND '
      '${COLUMN_PROJECT_SUBMISSION_USER_ID} = ? AND '
      '${COLUMN_PROJECT_SUBMISSION_PROJECT_ID} = ? AND '
      '${COLUMN_PROJECT_SUBMISSION_TIMESTAMP} = ?';

  static final String PRIMARY_KEY_WHERE_STRING_WITHOUTTIMESTAMP =
      '${COLUMN_PROJECT_SUBMISSION_APP_ID} = ? AND '
      '${COLUMN_PROJECT_SUBMISSION_USER_ID} = ? AND '
      '${COLUMN_PROJECT_SUBMISSION_PROJECT_ID} = ? AND '
      '${COLUMN_PROJECT_SUBMISSION_TIMESTAMP} = ';

}

class ProjectFormTableEntry implements BaseColumns {
  static const String TABLE_PROJECT_FORM = "project_form_table";
  static const String COLUMN_USER_ID = "user_id";
  static const String COLUMN_APP_ID = "app_id";
  static const String COLUMN_PROJECT_ID = "project_id";
  static const String COLUMN_FORM_TYPE = "form_type";
  static const String COLUMN_FORM_DATA = "form_data";
  static const String COLUMN_MD_INSTANCE_ID = "md_instance_id";
  static const String COLUMN_VERSION = "form_version";
  //Project Form Table
  static final String SQL_CREATE_PROJECT_FORM_TABLE = "CREATE TABLE " +
      TABLE_PROJECT_FORM +
      " (" +
      COLUMN_USER_ID +
      " TEXT," +
      COLUMN_APP_ID +
      " TEXT," +
      COLUMN_PROJECT_ID +
      " TEXT," +
      COLUMN_FORM_TYPE +
      " TEXT," +
      COLUMN_FORM_DATA +
      " TEXT," +
      COLUMN_VERSION +
      " INTEGER," +
      COLUMN_MD_INSTANCE_ID +
      " TEXT," +
      " PRIMARY KEY (" +
      COLUMN_USER_ID +
      ", " +
      COLUMN_APP_ID +
      "," +
      COLUMN_PROJECT_ID +
      "," +
      COLUMN_FORM_TYPE +
      "," +
      COLUMN_VERSION +
      ")" +
      ")";

  static final String SQL_DELETE_PROJECT_FORM_TABLE =
      "DROP TABLE IF EXISTS " + TABLE_PROJECT_FORM;

  String whereClause = COLUMN_USER_ID +
      " = ? AND " +
      COLUMN_APP_ID +
      " = ? AND " +
      COLUMN_PROJECT_ID +
      " = ? AND " +
      COLUMN_FORM_TYPE +
      " = ? AND " +
      COLUMN_VERSION +
      " = ?";
}

class EntityMetaEntry implements BaseColumns {
  static const String TABLE_ENTITY_METADATA = "entity_metadata";
  static const String COLUMN_SUPER_APP_ID = "super_app_id";
  static const String COLUMN_APP_ID = "app_id";
  static const String COLUMN_PROJECT_ID = "project_id";
  static const String COLUMN_USER_ID = "user_id";
  static const String COLUMN_PARENT_ENTITY = "parent_entity";
  static const String COLUMN_ENTITY_NAME = "entity_name";
  static const String COLUMN_ELEMENTS = "elements";
  static const String COLUMN_INSERT_TIMESTAMP = "insert_ts";
  //Entity config table
  static final String SQL_CREATE_ENTITY_META_TABLE = "CREATE TABLE " +
      TABLE_ENTITY_METADATA +
      " (" +
      COLUMN_SUPER_APP_ID +
      " TEXT," +
      COLUMN_APP_ID +
      " TEXT," +
      COLUMN_PROJECT_ID +
      " TEXT," +
      COLUMN_USER_ID +
      " TEXT," +
      COLUMN_PARENT_ENTITY +
      " TEXT," +
      COLUMN_ENTITY_NAME +
      " TEXT," +
      COLUMN_ELEMENTS +
      " TEXT," +
      COLUMN_INSERT_TIMESTAMP +
      " INTEGER," +
      " PRIMARY KEY (" +
      COLUMN_SUPER_APP_ID +
      "," +
      COLUMN_APP_ID +
      "," +
      COLUMN_USER_ID +
      "," +
      COLUMN_PROJECT_ID +
      "," +
      COLUMN_PARENT_ENTITY +
      "," +
      COLUMN_ENTITY_NAME +
      "))";

  static final String SQL_DELETE_ENTITY_META_TABLE =
      "DROP TABLE IF EXISTS " + TABLE_ENTITY_METADATA;

  static final String PRIMARY_KEY_WHERE_CLAUSE = COLUMN_SUPER_APP_ID +
      " = ? AND " +
      COLUMN_APP_ID +
      " = ? AND " +
      COLUMN_PROJECT_ID +
      " = ? AND " +
      COLUMN_USER_ID +
      " = ? AND " +
      COLUMN_PARENT_ENTITY +
      " = ? AND " +
      COLUMN_ENTITY_NAME +
      " = ?";
}
