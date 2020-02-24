package com.vassar.unifiedapp.utils;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;

import org.json.JSONException;
import org.json.JSONObject;

public interface Constants {

    String FRAMEWORK_VERSION = PropertyReader.getProperty("FRAMEWORK_VERSION");

    String SUPER_APP_ID = PropertyReader.getProperty("SUPER_APP_ID");

    String BASE_URL = PropertyReader.getProperty("BASE_URL");

    String FILE_PROVIDER = PropertyReader.getProperty("FILE_PROVIDER");

    //PreSyncBroadcast
    String PRE_SYNC_BROADCAST_ACTION = PropertyReader.getProperty("PRE_SYNC_BROADCAST_ACTION");

    // PostSyncBroadcast
    String POST_SYNC_BROADCAST_ACTION = PropertyReader.getProperty("POST_SYNC_BROADCAST_ACTION");

    // FailedSyncBroadcast
    String FAILED_SYNC_BROADCAST = PropertyReader.getProperty("FAILED_SYNC_BROADCAST");

    // FailedSyncBroadcast
    String LOGOUT_UPDATE_BROADCAST = PropertyReader.getProperty("LOGOUT_UPDATE_BROADCAST");

    int APP_DATABASE_VERSION = Integer.parseInt(PropertyReader.getProperty("APP_DATABASE_VERSION"));

    String APP_DATABASE_NAME = PropertyReader.getProperty("APP_DATABASE_NAME");

    String APP_DESCRIPTION = PropertyReader.getProperty("APP_DESCRIPTION");

    // FormDefaults
    int PROJECT_TYPE_FORM_VERSION_DEFAULT = 0;
    int ROOT_CONFIG_VERSION_DEFAULT = 0;

    // ArcGIS default level of detail
    int ARCGIS_DEFAULT_LEVEL_OF_DETAIL = 6;

    // Default max images for UI element
    int DEFAULT_MAX_IMAGES = 3;
    int DEFAULT_MAX_VIDEO = 30;

    // Default auto sync interval - in minutes
//    int DEFAULT_SYNC_INTERVAL = 30;

    // Default Service Frequency
    long DEFAULT_SERVICE_FREQUENCY = 60000; // 60 seconds

    String SERVICE_FREQUENCY_APP_MD_CONFIG = "appmetaconfig";
    String SERVICE_FREQUENCY_ROOT_CONFIG = "rootconfig";
    String SERVICE_FREQUENCY_MAP_CONFIG = "mapconfig";
    String SERVICE_FREQUENCY_PROJECT_TYPE = "projecttype";
    String SERVICE_FREQUENCY_PROJECT_LIST = "projectlist";
    String SERVICE_FREQUENCY_SERVER_ERROR = "servererrsync";
    String MEDIA_SYNC_FREQUENCY = "media";

    //Offline Map constants
    String DEFAULT_MAP_STORAGE = "/osmdroid";
    String MAP_DOWNLOAD_PREFERENCES = "MAP_DOWNLOAD_PREFERENCES";

    // Default project ID
    String DEFAULT_PROJECT_ID = "00000000-0000-0000-0000-000000000000";

    // Action Form Keys
    String UPDATE_FORM_KEY = "UPDATE";
    String INSERT_FORM_KEY = "INSERT";

    // Preferences
    String APP_PREFERENCES_KEY = "APP_PREFERENCES_KEY";
    int SPLASH_DURATION_DEFAULT = 2000;
    String SPLASH_VERSION_DEFAULT = "0";
    String USER_TOKEN_PREFERENCE_KEY = "USER_TOKEN_KEY";
    String USER_TOKEN_PREFERENCE_DEFAULT = null;
    String USER_ID_PREFERENCE_KEY = "USER_ID_PREFERENCE_KEY";
    String USER_ID_PREFERENCE_DEFAULT = null;
    String USER_IS_LOGGED_IN_PREFERENCE_KEY = "USER_IS_LOGGED_IN_PREFERENCE_KEY";
    String LOCALE_IN_PREFERENCE_KEY = "LOCALE_IN_PREFERENCE_KEY";
    boolean USER_IS_LOGGED_IN_PREFERENCE_DEFAULT = false;
   // Default UserId
    String DEFAULT_USER_ID = "default_user";
    // Config File Names
    String APP_META_CONFIG_DB_NAME = "appmetaconfig";
    String ROOT_CONFIG_DB_NAME = "rootConfig";
    String MAP_CONFIG_DB_NAME = "mapConfig";
    String PROJECT_TYPE_CONFIG_DB_NAME = "projecttypeconfig";
    String PROJECT_LIST_CONFIG_DB_NAME = "projectlistconfig";


    // API Request Parameters
    String APP_META_DATA_VERSION_KEY = "versionId";
    String APP_META_DATA_SUPER_APP_KEY = "superapp";

    String LOGIN_USERNAME_KEY = "mobile";
    String LOGIN_PASSWORD_KEY = "password";
    String LOGIN_SUPER_APP_KEY = "superapp";

    String ROOT_CONFIG_USER_ID_KEY = "userid";
    String ROOT_CONFIG_TOKEN_KEY = "token";
    String ROOT_CONFIG_SUPER_APP_KEY = "superapp";
    String ROOT_CONFIG_VERSION_KEY  = "version";

    String MAP_CONFIG_USER_ID_KEY = "userid";
    String MAP_CONFIG_TOKEN_KEY = "token";
    String MAP_CONFIG_SUPER_APP_KEY = "superapp";
    String MAP_CONFIG_VERSION_KEY  = "version";

    String PROJECT_TYPE_CONFIG_USER_ID_KEY = "userid";
    String PROJECT_TYPE_CONFIG_TOKEN_KEY = "token";
    String PROJECT_TYPE_CONFIG_SUPER_APP_KEY = "superapp";
    String PROJECT_TYPE_CONFIG_APP_ID_KEY = "appid";
    String PROJECT_TYPE_CONFIG_FORM_VERSION_KEY = "versionmap";
    String PROJECT_ID = "project_id";

    String PROJECT_LIST_CONFIG_USER_ID_KEY = "userid";
    String PROJECT_LIST_CONFIG_TOKEN_KEY = "token";
    String PROJECT_LIST_CONFIG_SUPER_APP_KEY = "superapp";
    String PROJECT_LIST_CONFIG_APP_ID_KEY = "appid";
    String PROJECT_LIST_CONFIG_MD_INSTANCE_ID_KEY = "md_instance_id";
    String PROJECT_LIST_CONFIF_PROJECT_ID_LIST_KEY = "project_list";

    String PROJECT_SUBMISSION_PARAMETER_KEY = "formdata";

    String LOGOUT_USER_ID_KEY = "userid";
    String LOGOUT_TOKEN_KEY = "token";
    String LOGOUT_SUPER_APP_KEY = "superapp";

    String PROJECT_SUBMIT_FORM_ID_KEY = "form_id";
    String PROJECT_SUBMIT_MD_INSTANCE_ID_KEY = "md_instance_id";
    String PROJECT_SUBMIT_PROJECT_ID_KEY = "proj_id";
    String PROJECT_SUBMIT_USER_TYPE_KEY = "user_type";
    String PROJECT_SUBMIT_INSERT_TS_KEY = "insert_ts";
    String PROJECT_SUBMIT_FIELDS_KEY = "fields";
    String PROJECT_SUBMIT_SUBMIT_DATA_KEY = "submit_data";
    String PROJECT_SUBMIT_SUPER_APP_KEY = "super_app";
    String PROJECT_SUBMIT_USER_ID_KEY = "user_id";
    String PROJECT_SUBMIT_TOKEN_KEY = "token";
    String PROJECT_SUBMIT_APP_ID_KEY = "app_id";
    String PROJECT_ADDITIONAL_PROPERTIES = "additional_props";

    String SEND_OTP_USERNAME = "user_id";
    String SEND_OTP_SUPER_APP = "super_app";

    String CHANGE_PASSWORD_USERNAME = "user_id";
    String CHANGE_PASSWORD_SUPER_APP = "super_app";
    String CHANGE_PASSWORD_OTP = "otp";
    String CHANGE_PASSWORD_PASSWORD = "new_password";

    String ENTITY_PARENT = "parent_entity";
    String ENTITY_NAME = "entity_name";

    // Error Messages
    String SOMETHING_WENT_WRONG = "Something went wrong!";
    String DATA_INITIALIZATION_FAILED = "Data initialization failed!";
    String NO_APPLICATIONS_FOR_USER = "No application data for this user!";
    String COULD_NOT_LOAD_FORM = "Could not load form. Login again and try!";
    String DATABASE_ERROR = "Cannot fetch information at the moment!";
    String NO_OFFLINE_USER_DATA = "Cannot login without network for this user";
    String NETWORK_ERROR = "Couldn't connect to the server!";
    String MISSING_CREDENTIALS = "Username or Password cannot be empty!";
    String MISSING_PASSWORD = "Please enter the password!";
    String MISSING_USERNAME = "Please enter the username!";
    String INCORRECT_CREDENTIALS = "Username or Password do not match!";
    String NO_OFFLINE_LOGOUT = "Cannot logout without network connection!";
    String CANNOT_USE_WITHOUT_PERMISSION = "Cannot use the application without granting permission";
    String NO_PROJECTS_TO_SUBMIT = "No projects to submit!";
    String NO_IMAGES_TAKEN = "No Images taken!";
    String NO_VIDEO_TAKEN = "No Video taken!";
    String GEOTAG_WASNT_SET = "Geotag not set!";
    String NO_MAP_DATA_AVAILABLE = "Map data not available for this application!";
    String MAX_IMAGES_REACHED = "Cannot take any more images for this form field!";
    String TAKE_THREE_IMAGES = "Please take all three images before previewing!";
    String NO_CAMERA_SUPPORTED = "Phone does not support camera feature!";
    String PROJECT_SUBMITTED_OFFLINE = "Project submitted offline!";
    String PROJECT_SUBMITTED = "Project submitted!";
    String INCONSISTENT_DATA = "Something went wrong! Please logout and login again.";
    String APP_SESSION_EXPIRED = "Cannot submit any more data in offline mode. Connect to network and try again!";
    String SUBMISSION_DATE_CROSSED = "Data for this project cannot be updated any longer!";
    String SUBMISSION_UNAVAILABLE = "Data for this project cannot be updated right now!";
    String PROJECT_ACCESS_INVOKED = "This project is not available for you!";
    String CHECK_INTERNET_CONNECTION = "Please check your internet connection!";
    String ERROR_BUT_SAVED_PROJECT_OFFLINE = "Something went wrong, but project submitted offline!";
    String BACKGROUND_SYNC_ALREADY_IN_PROGRESS = "Background sync is already in progress!";
    String SYNC_FAILED = "Sync Failed!";
    String VALIDATION_INITIALIZATION_FAILED = "Cannot find validating fields. Please refill the form fields!";
    String MANDATORY_FIELD_NOT_ENTERED = "Please enter all mandatory fields!";
    String MAX_VIDEO_LENGTH_REACHED = "Maximum recording length reached!!";
    String PASSWORDS_DONT_MATCH = "The passwords do not match!";
    String PASSWORD_CHANGED_SUCCESSFULLY = "Your password has been changed. Please login!";
    String GEOTAG_IMAGE_VALIDATION_FAILED = "Not a valid image according to location!";
    String TRY_AGAIN_LATER = "Initializing, try again later!";
    String NO_FILTERING_PARAMETERS = "No filters selected!";
    String NO_FILTERING_RESULTS = "No results for this filter";
    String DOWNLOAD_MAP = "First download the map!";
    String NO_DATA_FOR_SUBMISSION = "No data available for submission!";

    // Edu alerts
    String EDU_CAMERA_ALERT = "Capture the event photo in landscape mode";
    String EDU_CAMERA_LOCATION_ALERT = "Cannot update without Location turned on!";


    // List Layout Spans
    int PROJECT_TYPE_GRID_SPAN = 1;

    // Image Types
    String SPLASH_IMAGE_BACKGROUND = "splashBackground";
    String SPLASH_IMAGE_FOREGROUND = "splashForeground";
    String LOGIN_ICON_IMAGE = "loginIcon";
    String PROJECT_TYPE_ICON_IMAGE = "projecttypeicon";
    String MAP_GEOJSON = "mapGeojsonFile";
    String MAP_MARKERS = "mapMarkers";
    String MAP_ICONS = "mapIcons";

    // Permission Codes
    int PERMISSION_CAMERA = 101;
    int PERMISSION_WRITE_EXTERNAL_STORAGE = 100;
    int PERMISSION_FINE_LOCATION = 102;
    int PERMISSION_VIDEO = 103;
    int PERMISSION_AUDIO = 104;
    int REQUEST_LOCATION_SETTINGS = 105;
    int REQUEST_READ_PHONE_STATE = 106;

    // Activity Request Codes
    int REQUEST_IMAGE_CAPTURE = 500;
    int FORM_GET_IMAGES = 600;
    int SHOW_PROJECT_PREVIEW = 700;
    int REQUEST_GEOTAG_PIN_DROP = 800;
    int REQUEST_CHECK_SETTINGS = 900;
    int FORM_RECORD_VIDEO = 1000;
    int FILTER = 1100;
    int REQUEST_IMAGE_RESULT = 50;
    int PICK_FROM_GALLERY = 200;

    // Media service
    int BATCH_SIZE = 20;
    long UPLOAD_TIMESTAMP_DEFAULT = -999;
    long FORM_SUBMISSION_TIMESTAMP_DEFAULT = -999;
    int DEFAULT_RETRIES = 10;
    double DEFAULT_LONGITUDE = -1;
    double DEFAULT_LATITUDE = -1;
    float DEFAULT_ACCURACY = -1;
    float DEFAULT_BEARING = -1;
    String UPLOAD_SERVICE_URL = "submitimage";
    String DOWNLOAD_SERVICE_URL = "downloadimage";
    String IMAGE_EXT = "jpg";
    String VIDEO_EXT = "mp4";
    int MEDIA_SYNC_INTERVAL = 300000;
    int TEXT_DATA_SYNC_INTERVAL = 300000;
    String TEXT_DATA_INTERVAL = "TEXT_DATA_INTERVAL";
    String MEDIA_LOCATION_BEARING = "media_location_bearing";


 // SPEL Expression Constants
    String SPEL_PROJECT_TS = "form_submit_ts"; // Project submission timestamp
    String SPEL_PROJECT_TIME_IN_SECONDS = "form_submit_time"; // Time in seconds from midnight

    // NA Values
    String NA_STRING = "N/A";
    String IMAGE_UUID_LONG_LAT_SEPARATOR = "##";

    // Sorting
    String PROJECT_TYPE_ALPHABETICAL_SORTING = "ALPHABETICAL";
    String PROJECT_TYPE_CUSTOM_SORTING = "CUSTOM";

    String PROJECT_LIST_ALPHABETICAL_SORTING = "ALPHABETICAL";
    String PROJECT_LIST_LAST_UPDATED_ASCENDING_SORTING = "LAST_UPDATED_ASC";
    String PROJECT_LIST_LAST_UPDATED_DESCENDING_SORTING = "LAST_UPDATED_DESC";
    String PROJECT_LIST_NEAREST_PROJECTS_FIRST = "NEAREST_PROJECTS_FIRST";

    // Filtering
    String FILTER_APP_ID = "filterAppId";
    String FILTER_USER_ID = "filterUserId";
    String FILTER_ROOT_CONFIG = "filterRootConfig";


    // OnSaveInstanceStateVariables
    // Home Activity
    String HOME_USER_ID = "userId";
    String HOME_USER_TOKEN = "userToken";
    String HOME_ROOT_CONFIG_STRING = "rootconfigstring";
    String HOME_FLATTENED_APP_LIST = "flattenedAppList";
    String HOME_CHILD_PROJECT_TYPES = "childProjectTypes";
    String HOME_ROOT_CONFIG = "rootConfig";
    String HOME_APP_META_CONFIG = "appMetaConfig";
    // Grid Fragment
    String GRID_FRAGMENT_ROOT_PROJECT_TYPES = "rootProjectTypes";
    String GRID_FRAGMENT_PROJECT_TYPES = "projectTypes";
    String GRID_FRAGMENT_CHILD_PROJECT_TYPES = "childProjectTypes";
    String GRID_FRAGMENT_USER_ID = "userId";
    // Project List Activity
    String PROJECT_LIST_APP_ID = "appId";
    String PROJECT_LIST_GROUPING_ATTRIBUTE = "groupingAttribute";
    String PROJECT_LIST_GROUPING_ATTRIBUTE_VALUE = "groupingAttributeValue";
    String PROJECT_LIST_USER_ID = "userId";
    String PROJECT_LIST_USER_TYPE = "userType";
    String PROJECT_LIST_CONFIG_STRING = "projectListConfigString";
    String PROJECT_LIST_CONFIG = "projectListConfig";
    String PROJECT_LIST_PROJECTS = "projectListProjects";
    String PROJECT_LIST_CREATION_TIMESTAMP = "projectListCreationTimestamp";
    String PROJECT_LIST_DEFAULT_MAP_ICON = "DEFAULT";
    String PROJECT_LIST_MAP_ELEMENT_LABEL = "label";
    String PROJECT_LIST_MAP_ELEMENT_INDEX = "index";
    String PROJECT_LIST_MAP_ELEMENT_ICON_URL = "icon_url";
    String PROJECT_LATITUDE = "proj_lat";
    String PROJECT_LONGITUDE = "proj_lon";

    // Project List Fragment
    String PROJECT_LIST_FRAGMENT_PROJECTS = "projectListFragmentProjects";
    String PROJECT_LIST_FRAGMENT_CREATION_TIMESTAMP = "projectListFragmentCreationTimestamp";
    // Project Grouping Activity
    String PROJECT_GROUPING_APP_ID = "PROJECT_GROUPING_APP_ID";
    String PROJECT_GROUPING_ATTRIBUTES = "PROJECT_GROUPING_ATTRIBUTES";
    String PROJECT_GROUPING_ATTRIBUTE = "PROJECT_GROUPING_ATTRIBUTE";

    // Error logs
    String LOG_UNIAPP_ERROR = "LOG_UNIAPP_ERROR";
    String APP_NA = "APPLICATION_CLASS_NOT_AVAILABLE";
    String APP_META_DATA_NA = "APP_META_DATA_NOT_AVAILABLE";

    // Server session expiry
    long SERVER_SESSION_EXPIRY = 30;

    String NO_NEW_APP_MD = "NO_NEW_APP_MD";
    String NO_NEW_ROOT_CONFIG = "NO_NEW_ROOT_CONFIG";
    String NO_NEW_MAP_CONFIG = "NO_NEW_MAP_CONFIG";
    String NO_NEW_LOCALIZATION_CONFIG = "NO_NEW_LOCALIZATION_CONFIG";
    // Project Submission Response Constants
    String PROJECT_RESPONSE_STATUS_CODE = "status";
    String PROJECT_RESPONSE_IS_SUCCESSFUL = "success";
    String PROJECT_RESPONSE_MESSAGE = "message";

    String SUBMISSION_FIELD_ARRAY = "fields";

    String SUBMISSION_APP_VERSION_KEY = "app_version";
    int DEFAULT_APP_ERROR_CODE = -1;
    String USER_TOKEN = "user_token";
    // Intent list to ProjectList
    String PROJECT_LIST_INTENT_APP_ID = "PROJECT_LIST_INTENT_APP_ID";
    String PROJECT_LIST_INTENT_USER_TYPE = "PROJECT_LIST_INTENT_USER_TYPE";
    String PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE = "PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE";
    String PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE_VALUE = "PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE_VALUE";

    // Intent to ProjectGroupingActivity
    String GROUPING_INTENT_APP_ID = "GROUPING_INTENT_APP_ID";
    String GROUPING_INTENT_GROUPING_PARAMETER = "GROUPING_INTENT_GROUPING_PARAMETER";
    String PROJECT_TYPE_NAME = "PROJECT_TYPE_NAME";


    // Intent list to ProjectForm
    String PROJECT_FORM_INTENT_APP_ID = "PROJECT_FORM_INTENT_APP_ID";
    String PROJECT_FORM_INTENT_PROJECT_ID = "PROJECT_FORM_INTENT_PROJECT_ID";
    String FORM_ACTION_TYPE = "FORM_ACTION_TYPE";
    String PROJECT_FORM_INTENT_PROJECT_NAME = "PROJECT_FORM_INTENT_PROJECT_NAME";

    String IMAGE_TYPE_JPG = ".jpg";
    String VIDEO_TYPE_MP4 = ".mp4";

    String SUBMISSION_ERROR = "An error occured while submission, please try again";

    int DATABASE_INSERT_ERROR_CODE = -1;

    int DEFAULT_UPLOAD_STATUS = -999;

    String PROJECT_SUBMISSION_ENTRY_DEFAULT_SERVER_RESPONSE = "";
    int PROJECT_SUBMISSION_ENTRY_DEFAULT_SERVER_SYNC_TS = 0;
    int PROJECT_SUBMISSION_ENTRY_DEFAULT_RETRY_COUNT = 0;
    int DEFAULT_SYNC_INTERVAL = 900000;
//    String IMAGE_UUID_LONG_LAT_SEPARATOR = "##";

    String DEFAULT_DELIMITER = "##";

    // onStartUp
    String ON_STARTUP_DEFAULT = "DEFAULT";
    String ON_STARTUP_ACTIVITY = "ACTIVITY";
    String ON_STARTUP_SERVICE = "SERVICE";

    // DynamicForm Constants
    String FORM_SHOULD_SHOW_TOOLBAR = "shouldShowToolbar";

    // OnStartup SharedPreferences
    String ON_STARTUP_DATA_EXISTS = "ON_STARTUP_DATA_EXISTS";
    String ON_STARTUP_DATA = "ON_STARTUP_DATA";
    String ON_STARTUP_DATA_ARRAY = "ON_STARTUP_DATA_ARRAY";
    String TOKEN_EXPIRY_MESSAGE = "Token is expired. Please login.";

    String MOVE_TO_HOME = "MOVE_TO_HOME";

    // Tabular form Constants
    String TABULAR_FORM_TABLE_FIELD = "table";
    String TABULAR_FORM_NEW_PAGE_TABLE_FIELD = "new_page_table";
    String TABULAR_FORM_TEXT_FIELD = "textview";
    String TABULAR_FORM_RADIO_FIELD = "radio";
    String TABULAR_FORM_EDITTEXT_FIELD = "edittext";
    String TABULAR_FORM_SPINNER_FIELD = "dropdown";
    String TABULAR_FORM_COMPUTED_TEXTVIEW = "computed_textview";
    String TABULAR_FORM_GEOTAGGED_IMAGE_FUSED_FIELD = "geotagimagefused";
    String TABULAR_FORM_ADDENDUM_FIELD = "addendum";
    String TABULAR_FORM_DATE_FIELD = "date";
    String TABULAR_FORM_CHECKBOX = "checkbox";

    String VIEW_ALIGNMENT_LEFT = "left";
    String VIEW_ALIGNMENT_CENTER = "center";
    String VIEW_ALIGNMENT_RIGHT = "right";

    String VIEW_LABEL_STYLE_BOLD = "bold";

    String SUBMISSION_OBJECT_KEY = "key";
    String SUBMISSION_OBJECT_DT = "dt";
    String SUBMISSION_OBJECT_VAL = "val";

    String IMAGE_PREVIEW_INTENT_EXTRA_IMAGE_PATH = "image_path";
    String NO_DATA_AVAILABLE = "No data available!";

    String APP_ID_TO_GROUPING_ATTRIBUTE_MAP_PREFERENCE_KEY = "APP_ID_TO_GROUPING_ATTRIBUTE_MAP_PREFERENCE_KEY";
    String NO_PROJECTS_FOUND = "No results found. Please select appropriate filters.";
    double NA_DOUBLE = -999;

    String LATITUDE_KEY =  "lat";
    String LONGITUDE_KEY =  "lon";

    String MENU_FILTERING_ICON = "MENU_FILTERING_ICON";
    String MENU_MAP_ICON = "MENU_MAP_ICON";
    String FORGOT_PASSWORD = "FORGOT_PASSWORD";
    String MENU_PROFILE_ICON = "MENU_PROFILE_ICON";
    String SELECTED_FILTER_KEY_VALUE_MAP = "SELECTED_FILTER_KEY_VALUE_MAP";

    String ROOT_CONFIG_APPLICATION_STATUS_INACTIVE = "inactive";
    String ROOT_CONFIG_APPLICATION_STATUS_INACTIVE_MESSAGE = "This application is not active at the moment. Please check again later.";

    static String capitalize(String str) {
        String[] words = str.split("\\s");
        StringBuilder sb = new StringBuilder();

        for (String s: words) {
            if (!s.equals("")) {
                sb.append(Character.toUpperCase(s.charAt(0)));
                sb.append(s.substring(1));
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

//    JSONObject translatedJson = new JSONObject();
//    static String translate(String str) {
//        String translatedString = UAAppContext.getInstance().getContext().getResources().getString(R.string.kml_style_styleID);
//        try {
//            translatedJson.put("PROJECT_GROUPING_APP_ID","PROJECT_GROUPING_APP_ID_HI");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return translatedString;
//    }
}
