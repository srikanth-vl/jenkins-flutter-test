class CommonConstants {
  static const String BASE_URL = 'http://138.68.30.58:9000/api/uniapp/';
  // PRRD
  // static const String SUPER_APP_ID = "bf7b665a-39a0-34c9-b730-719aad9f9d1c";

  //BLUIS
  static const String SUPER_APP_ID = "874854c0-e527-31a0-9bcc-397772c7a148";

  //Uniapp Framework App
  // static const String SUPER_APP_ID = "33c7ed03-48dc-4421-b07d-0ae773414e9e";

  // HAIMS
  //   static const String SUPER_APP_ID = "9e49254b-d85e-11e9-beb7-b5d18d261495";

  // Default Appbar title for the app.
  // static const String DEFAULT_APPBAR_TITLE = "app_name";
  static const String DEFAULT_APPBAR_TITLE = "Vassar_labs";

  static const String APP_LOGO = "assets/images/logo.png";
  static const String APP_INVERTED_LOGO = "assets/images/logo-white.png";
  
  static const String DEFAULT_APP_LOGO = "assets/images/default_logo.png";
  static const String PREFERENCE_USER_LOGO = "assets/images/user1.png";
  static const String NO_DATA_TO_DISPLAY_IMAGE =
      "assets/images/no_data_found.png";

  // The toggle property to change the theme
  static const bool DEFAULT_THEME = false;

  static const String DB_NAME = "uniapp.db";
  static const int DB_VERSION = 1;
  static const String APP_META_CONFIG_NAME = "APP_META_CONFIG";
  static const String ROOT_CONFIG_NAME = "ROOT_CONFIG";
  static const String DEFAULT_USER = "0000";
  static int PROJECT_TYPE_FORM_VERSION_DEFAULT = 0;
  static int ROOT_CONFIG_VERSION_DEFAULT = 0;

  // ArcGIS default level of detail
  static int ARCGIS_DEFAULT_LEVEL_OF_DETAIL = 6;

  // Default max images for UI element
  static int DEFAULT_MAX_IMAGES = 3;
  static int DEFAULT_MAX_VIDEO = 30;

  // Default auto sync interval - in minutes
  //  static int DEFAULT_SYNC_INTERVAL = 30;

  // Default Service Frequency
  static int DEFAULT_SERVICE_FREQUENCY = 60000; // 60 seconds

  static const String SERVICE_FREQUENCY_APP_MD_CONFIG = "appmetaconfig";
  static const String SERVICE_FREQUENCY_ROOT_CONFIG = "rootconfig";
  static const String SERVICE_FREQUENCY_MAP_CONFIG = "mapconfig";
  static const String SERVICE_FREQUENCY_PROJECT_TYPE = "projecttype";
  static const String SERVICE_FREQUENCY_PROJECT_LIST = "projectlist";
  static const String SERVICE_FREQUENCY_SERVER_ERROR = "servererrsync";
  static const String MEDIA_SYNC_FREQUENCY = "media";

  // Flutter Shared Preferences
  static const String USERNAME_SHARED_PREFERENCE = "username_pref";
  static const String TOKEN_SHARED_PREFERENCE = "token_pref";
  static const String IS_LOGGED_IN_SHARED_PREFERENCE = "is_logged_in_pref";

  // Preference DataTypes
  static const String PREFERENCE_TYPE_STRING = "string";
  static const String PREFERENCE_TYPE_STRING_LIST = "stringList";
  static const String PREFERENCE_TYPE_INT = "int";
  static const String PREFERENCE_TYPE_BOOL = "bool";
  static const String PREFERENCE_TYPE_DOUBLE = "double";

  //Offline Map constants
  static const String DEFAULT_MAP_STORAGE = "/osmdroid";
  static const String MAP_DOWNLOAD_PREFERENCES = "MAP_DOWNLOAD_PREFERENCES";

  // Default project ID
  static const String DEFAULT_PROJECT_ID =
      "00000000-0000-0000-0000-000000000000";

  // Action Form Keys
  static const String UPDATE_FORM_KEY = "UPDATE";
  static const String INSERT_FORM_KEY = "INSERT";

  // Preferences
  static const String APP_PREFERENCES_KEY = "APP_PREFERENCES_KEY";
  static int SPLASH_DURATION_DEFAULT = 2000;
  static const String SPLASH_VERSION_DEFAULT = "0";
  static const String USER_TOKEN_PREFERENCE_KEY = "USER_TOKEN_KEY";
  static const String USER_TOKEN_PREFERENCE_DEFAULT = null;
  static const String USER_ID_PREFERENCE_KEY = "USER_ID_PREFERENCE_KEY";
  static const String USER_ID_PREFERENCE_DEFAULT = null;
  static const String USER_IS_LOGGED_IN_PREFERENCE_KEY =
      "USER_IS_LOGGED_IN_PREFERENCE_KEY";
  static const String LOCALE_IN_PREFERENCE_KEY = "LOCALE_IN_PREFERENCE_KEY";
  bool USER_IS_LOGGED_IN_PREFERENCE_DEFAULT = false;
  // Default UserId
  static const String DEFAULT_USER_ID = "default_user";
  static const String GUEST_USER_ID = "00000";
  static const String GUEST_PASSWORD = "vassar";

  // Config File Names
  static const String APP_META_CONFIG_DB_NAME = "appmetaconfig";
  static const String ROOT_CONFIG_DB_NAME = "rootConfig";
  static const String MAP_CONFIG_DB_NAME = "mapConfig";
  static const String PROJECT_TYPE_CONFIG_DB_NAME = "projecttypeconfig";
  static const String PROJECT_LIST_CONFIG_DB_NAME = "projectlistconfig";

  // API Request Parameters
  static const String APP_META_DATA_VERSION_KEY = "versionId";
  static const String APP_META_DATA_SUPER_APP_KEY = "superapp";

  static const String LOGIN_USERNAME_KEY = "mobile";
  static const String LOGIN_PASSWORD_KEY = "password";
  static const String LOGIN_SUPER_APP_KEY = "superapp";

  static const String ROOT_CONFIG_USER_ID_KEY = "userid";
  static const String ROOT_CONFIG_TOKEN_KEY = "token";
  static const String ROOT_CONFIG_SUPER_APP_KEY = "superapp";
  static const String ROOT_CONFIG_VERSION_KEY = "version";

  static const String MAP_CONFIG_USER_ID_KEY = "userid";
  static const String MAP_CONFIG_TOKEN_KEY = "token";
  static const String MAP_CONFIG_SUPER_APP_KEY = "superapp";
  static const String MAP_CONFIG_VERSION_KEY = "version";

  static const String PROJECT_TYPE_CONFIG_USER_ID_KEY = "userid";
  static const String PROJECT_TYPE_CONFIG_TOKEN_KEY = "token";
  static const String PROJECT_TYPE_CONFIG_SUPER_APP_KEY = "superapp";
  static const String PROJECT_TYPE_CONFIG_APP_ID_KEY = "appid";
  static const String PROJECT_TYPE_CONFIG_FORM_VERSION_KEY = "versionmap";
  static const String PROJECT_ID = "project_id";

  static const String PROJECT_LIST_CONFIG_USER_ID_KEY = "userid";
  static const String PROJECT_LIST_CONFIG_TOKEN_KEY = "token";
  static const String PROJECT_LIST_CONFIG_SUPER_APP_KEY = "superapp";
  static const String PROJECT_LIST_CONFIG_APP_ID_KEY = "appid";
  static const String PROJECT_LIST_CONFIG_MD_INSTANCE_ID_KEY = "md_instance_id";
  static const String PROJECT_LIST_CONFIF_PROJECT_ID_LIST_KEY = "project_list";

  static const String PROJECT_SUBMISSION_PARAMETER_KEY = "formdata";

  static const String LOGOUT_USER_ID_KEY = "userid";
  static const String LOGOUT_TOKEN_KEY = "token";
  static const String LOGOUT_SUPER_APP_KEY = "superapp";

  static const String PROJECT_SUBMIT_FORM_ID_KEY = "form_id";
  static const String PROJECT_SUBMIT_MD_INSTANCE_ID_KEY = "md_instance_id";
  static const String PROJECT_SUBMIT_PROJECT_ID_KEY = "proj_id";
  static const String PROJECT_SUBMIT_USER_TYPE_KEY = "user_type";
  static const String PROJECT_SUBMIT_INSERT_TS_KEY = "insert_ts";
  static const String PROJECT_SUBMIT_FIELDS_KEY = "fields";
  static const String PROJECT_SUBMIT_SUBMIT_DATA_KEY = "submit_data";
  static const String PROJECT_SUBMIT_SUPER_APP_KEY = "super_app";
  static const String PROJECT_SUBMIT_USER_ID_KEY = "user_id";
  static const String PROJECT_SUBMIT_TOKEN_KEY = "token";
  static const String PROJECT_SUBMIT_APP_ID_KEY = "app_id";
  static const String PROJECT_ADDITIONAL_PROPERTIES = "additional_props";

  static const String SEND_OTP_USERNAME = "user_id";
  String SEND_OTP_SUPER_APP = "super_app";

  static const String CHANGE_PASSWORD_USERNAME = "user_id";
  static const String CHANGE_PASSWORD_SUPER_APP = "super_app";
  static const String CHANGE_PASSWORD_OTP = "otp";
  static const String CHANGE_PASSWORD_PASSWORD = "new_password";

  static const String ENTITY_CONFIG_REQUEST_PARENT_KEY = "parent_entity";
  static const String ENTITY_CONFIG_REQUEST_ENTITY_NAME_KEY = "entity_name";
  static const String ENTITY_CONFIG_REQUEST_USER_ID_KEY = "user_id";
  static const String ENTITY_CONFIG_REQUEST_TOKEN_KEY = "token";
  static const String ENTITY_CONFIG_REQUEST_SUPER_APP_KEY = "super_app";
  static const String ENTITY_CONFIG_REQUEST_APP_ID_KEY = "app_id";
  // Error Messages
  static const String SOMETHING_WENT_WRONG = "Something went wrong!";
  static const String USER_NOT_FOUND = "User not Found";
  static const String USER_PASSWORD_MISMATCH = "User Password Mismatch";
  static const String DATA_INITIALIZATION_FAILED =
      "Data initialization failed!";
  static const String NO_APPLICATIONS_FOR_USER =
      "No application data for this user!";
  static const String COULD_NOT_LOAD_FORM =
      "Could not load form. Login again and try!";
  static const String DATABASE_ERROR =
      "Cannot fetch information at the moment!";
  static const String NO_OFFLINE_USER_DATA =
      "Cannot login without network for this user";
  static const String NETWORK_ERROR = "Couldn't connect to the server!";
  static const String NETWORK_UNAVAILABLE =
      "You don't have access to internet, Please connect to network!";
  static const String MISSING_CREDENTIALS =
      "Username or Password cannot be empty!";
  static const String MISSING_PASSWORD = "Please enter the password!";
  static const String MISSING_USERNAME = "Please enter the username!";
  static const String INCORRECT_CREDENTIALS =
      "Username or Password do not match!";
  static const String NO_OFFLINE_LOGOUT =
      "Cannot logout without network connection!";
  static const String CANNOT_USE_WITHOUT_PERMISSION =
      "Cannot use the application without granting permission";
  static const String NO_PROJECTS_TO_SUBMIT = "No projects to submit!";
  static const String NO_IMAGES_TAKEN = "No Images taken!";
  static const String NO_VIDEO_TAKEN = "No Video taken!";
  static const String GEOTAG_WASNT_SET = "Geotag not set!";
  static const String NO_MAP_DATA_AVAILABLE =
      "Map data not available for this application!";
  static const String MAX_IMAGES_REACHED =
      "Cannot take any more images for this form field!";
  static const String TAKE_THREE_IMAGES =
      "Please take all three images before previewing!";
  static const String NO_CAMERA_SUPPORTED =
      "Phone does not support camera feature!";
  static const String PROJECT_SUBMITTED_OFFLINE = "Project submitted offline!";
  static const String PROJECT_SUBMITTED = "Project submitted!";
  static const String INCONSISTENT_DATA =
      "Something went wrong! Please logout and login again.";
  static const String APP_SESSION_EXPIRED =
      "Cannot submit any more data in offline mode. Connect to network and try again!";
  static const String SUBMISSION_DATE_CROSSED =
      "Data for this project cannot be updated any longer!";
  static const String SUBMISSION_UNAVAILABLE =
      "Data for this project cannot be updated right now!";
  static const String PROJECT_ACCESS_INVOKED =
      "This project is not available for you!";
  static const String CHECK_INTERNET_CONNECTION =
      "Please check your internet connection!";
  static const String ERROR_BUT_SAVED_PROJECT_OFFLINE =
      "Something went wrong, but project submitted offline!";
  static const String BACKGROUND_SYNC_ALREADY_IN_PROGRESS =
      "Background sync is already in progress!";
  static const String SYNC_FAILED = "Sync Failed!";
  static const String VALIDATION_INITIALIZATION_FAILED =
      "Cannot find validating fields. Please refill the form fields!";
  static const String MANDATORY_FIELD_NOT_ENTERED =
      "Please enter all mandatory fields!";
  static const String MAX_VIDEO_LENGTH_REACHED =
      "Maximum recording length reached!!";
  static const String PASSWORDS_DONT_MATCH = "The passwords do not match!";
  static const String PASSWORD_CHANGED_SUCCESSFULLY =
      "Your password has been changed. Please login!";
  static const String GEOTAG_IMAGE_VALIDATION_FAILED =
      "Not a valid image according to location!";
  static const String TRY_AGAIN_LATER = "Initializing, try again later!";
  static const String NO_FILTERING_PARAMETERS = "No filters selected!";
  static const String NO_FILTERING_RESULTS = "No results for this filter";
  static const String DOWNLOAD_MAP = "First download the map!";
  static const String NO_DATA_FOR_SUBMISSION =
      "No data available for submission!";

  // Edu alerts
  static const String EDU_CAMERA_ALERT =
      "Capture the event photo in landscape mode";
  static const String EDU_CAMERA_LOCATION_ALERT =
      "Cannot update without Location turned on!";

  // List Layout Spans
  static int PROJECT_TYPE_GRID_SPAN = 1;

  // Image Types
  static const String SPLASH_IMAGE_BACKGROUND = "splashBackground";
  static const String SPLASH_IMAGE_FOREGROUND = "splashForeground";
  static const String LOGIN_ICON_IMAGE = "loginIcon";
  static const String PROJECT_TYPE_ICON_IMAGE = "projecttypeicon";
  static const String MAP_GEOJSON = "mapGeojsonFile";
  static const String MAP_MARKERS = "mapMarkers";
  static const String MAP_ICONS = "mapIcons";

  // Permission Codes
  static int PERMISSION_CAMERA = 101;
  static int PERMISSION_WRITE_EXTERNAL_STORAGE = 100;
  static int PERMISSION_FINE_LOCATION = 102;
  static int PERMISSION_VIDEO = 103;
  static int PERMISSION_AUDIO = 104;
  static int REQUEST_LOCATION_SETTINGS = 105;
  static int REQUEST_READ_PHONE_STATE = 106;

  // Activity Request Codes
  static int REQUEST_IMAGE_CAPTURE = 500;
  static int FORM_GET_IMAGES = 600;
  static int SHOW_PROJECT_PREVIEW = 700;
  static int REQUEST_GEOTAG_PIN_DROP = 800;
  static int REQUEST_CHECK_SETTINGS = 900;
  static int FORM_RECORD_VIDEO = 1000;
  static int FILTER = 1100;
  static int REQUEST_IMAGE_RESULT = 50;
  static int PICK_FROM_GALLERY = 200;

  // Media service
  static int BATCH_SIZE = 20;
  static int UPLOAD_TIMESTAMP_DEFAULT = -999;
  static int FORM_SUBMISSION_TIMESTAMP_DEFAULT = -999;
  static int DEFAULT_RETRIES = 10;
  static double DEFAULT_LONGITUDE = -1;
  static double DEFAULT_LATITUDE = -1;
  static double DEFAULT_ACCURACY = -1;
  static double DEFAULT_BEARING = -1;
  static const String UPLOAD_SERVICE_URL = "submitimage";
  static const String DOWNLOAD_SERVICE_URL = "downloadimage";
  static const String IMAGE_EXT = "jpg";
  static const String VIDEO_EXT = "mp4";
  static const int MEDIA_SYNC_INTERVAL = 300000;
  static const int MEDIA_IMAGE_DEFAULT_RETRIES = 20;
  static const int TEXT_DATA_SYNC_INTERVAL = 300000;
  static const String TEXT_DATA_INTERVAL = "TEXT_DATA_INTERVAL";
  static const String MEDIA_LOCATION_BEARING = "media_location_bearing";

  // SPEL Expression Constants
  static String SPEL_PROJECT_TS =
      "form_submit_ts"; // Project submission timestamp
  static String SPEL_PROJECT_TIME_IN_SECONDS =
      "form_submit_time"; // Time in seconds from midnight

  // NA Values
  static String NA_String = "N/A";
  static String IMAGE_UUID_LONG_LAT_SEPARATOR = "##";

  // Sorting
  static const String PROJECT_TYPE_ALPHABETICAL_SORTING = "ALPHABETICAL";
  static const String PROJECT_TYPE_CUSTOM_SORTING = "CUSTOM";

  static const String PROJECT_LIST_ALPHABETICAL_SORTING = "ALPHABETICAL";
  static const String PROJECT_LIST_LAST_UPDATED_ASCENDING_SORTING =
      "LAST_UPDATED_ASC";
  static const String PROJECT_LIST_LAST_UPDATED_DESCENDING_SORTING =
      "LAST_UPDATED_DESC";
  static const String PROJECT_LIST_NEAREST_PROJECTS_FIRST =
      "NEAREST_PROJECTS_FIRST";

  // Filtering
  static String FILTER_APP_ID = "filterAppId";
  static String FILTER_USER_ID = "filterUserId";
  static String FILTER_ROOT_CONFIG = "filterRootConfig";

  // OnSaveInstanceStateVariables
  // Home Activity
  static String HOME_USER_ID = "userId";
  static String HOME_USER_TOKEN = "userToken";
  static String HOME_ROOT_CONFIG_String = "rootconfigString";
  static String HOME_FLATTENED_APP_LIST = "flattenedAppList";
  static String HOME_CHILD_PROJECT_TYPES = "childProjectTypes";
  static String HOME_ROOT_CONFIG = "rootConfig";
  static String HOME_APP_META_CONFIG = "appMetaConfig";
  // Grid Fragment
  static String GRID_FRAGMENT_ROOT_PROJECT_TYPES = "rootProjectTypes";
  static String GRID_FRAGMENT_PROJECT_TYPES = "projectTypes";
  static String GRID_FRAGMENT_CHILD_PROJECT_TYPES = "childProjectTypes";
  static String GRID_FRAGMENT_USER_ID = "userId";
  // Project List Activity
  static String PROJECT_LIST_APP_ID = "appId";
  static String PROJECT_LIST_GROUPING_ATTRIBUTE = "groupingAttribute";
  static String PROJECT_LIST_GROUPING_ATTRIBUTE_VALUE =
      "groupingAttributeValue";
  static String PROJECT_LIST_USER_ID = "userId";
  static String PROJECT_LIST_USER_TYPE = "userType";
  static String PROJECT_LIST_CONFIG_String = "projectListConfigString";
  static String PROJECT_LIST_CONFIG = "projectListConfig";
  static String PROJECT_LIST_PROJECTS = "projectListProjects";
  static String PROJECT_LIST_CREATION_TIMESTAMP =
      "projectListCreationTimestamp";
  static String PROJECT_LIST_DEFAULT_MAP_ICON = "DEFAULT";
  static String PROJECT_LIST_MAP_ELEMENT_LABEL = "label";
  static String PROJECT_LIST_MAP_ELEMENT_INDEX = "index";
  static String PROJECT_LIST_MAP_ELEMENT_ICON_URL = "icon_url";
  static String PROJECT_LATITUDE = "proj_lat";
  static String PROJECT_LONGITUDE = "proj_lon";

  // Project List Fragment
  static String PROJECT_LIST_FRAGMENT_PROJECTS = "projectListFragmentProjects";
  static String PROJECT_LIST_FRAGMENT_CREATION_TIMESTAMP =
      "projectListFragmentCreationTimestamp";
  // Project Grouping Activity
  static String PROJECT_GROUPING_APP_ID = "PROJECT_GROUPING_APP_ID";
  static String PROJECT_GROUPING_ATTRIBUTES = "PROJECT_GROUPING_ATTRIBUTES";
  static String PROJECT_GROUPING_ATTRIBUTE = "PROJECT_GROUPING_ATTRIBUTE";

  // Error logs
  static String LOG_UNIAPP_ERROR = "LOG_UNIAPP_ERROR";
  static String APP_NA = "APPLICATION_CLASS_NOT_AVAILABLE";
  static String APP_META_DATA_NA = "APP_META_DATA_NOT_AVAILABLE";

  // Server session expiry
  static int SERVER_SESSION_EXPIRY = 30;

  static String NO_NEW_APP_MD = "NO_NEW_APP_MD";
  static String NO_NEW_ROOT_CONFIG = "NO_NEW_ROOT_CONFIG";
  static String NO_NEW_MAP_CONFIG = "NO_NEW_MAP_CONFIG";
  static String NO_NEW_LOCALIZATION_CONFIG = "NO_NEW_LOCALIZATION_CONFIG";
  // Project Submission Response Constants
  static String PROJECT_RESPONSE_STATUS_CODE = "status";
  static String PROJECT_RESPONSE_IS_SUCCESSFUL = "success";
  static String PROJECT_RESPONSE_MESSAGE = "message";

  static String SUBMISSION_FIELD_ARRAY = "fields";

  static String SUBMISSION_APP_VERSION_KEY = "app_version";
  static int DEFAULT_APP_ERROR_CODE = -1;
  static String USER_TOKEN = "user_token";
  // Intent list to ProjectList
  static String PROJECT_LIST_INTENT_APP_ID = "PROJECT_LIST_INTENT_APP_ID";
  static String PROJECT_LIST_INTENT_USER_TYPE = "PROJECT_LIST_INTENT_USER_TYPE";
  static String PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE =
      "PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE";
  static String PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE_VALUE =
      "PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE_VALUE";

  // Intent to ProjectGroupingActivity
  static String GROUPING_INTENT_APP_ID = "GROUPING_INTENT_APP_ID";
  static String GROUPING_INTENT_GROUPING_PARAMETER =
      "GROUPING_INTENT_GROUPING_PARAMETER";
  static String PROJECT_TYPE_NAME = "PROJECT_TYPE_NAME";

  // Intent list to ProjectForm
  static String PROJECT_FORM_INTENT_APP_ID = "PROJECT_FORM_INTENT_APP_ID";
  static String PROJECT_FORM_INTENT_PROJECT_ID =
      "PROJECT_FORM_INTENT_PROJECT_ID";
  static String FORM_ACTION_TYPE = "FORM_ACTION_TYPE";
  static String PROJECT_FORM_INTENT_PROJECT_NAME =
      "PROJECT_FORM_INTENT_PROJECT_NAME";

  static String IMAGE_TYPE_JPG = ".jpg";
  static String VIDEO_TYPE_MP4 = ".mp4";

  static String SUBMISSION_ERROR =
      "An error occured while submission, please try again";

  static const int DATABASE_INSERT_ERROR_CODE = -1;

  static const int DEFAULT_UPLOAD_STATUS = -999;

  static const String PROJECT_SUBMISSION_ENTRY_DEFAULT_SERVER_RESPONSE = "";
  static const int PROJECT_SUBMISSION_ENTRY_DEFAULT_SERVER_SYNC_TS = 0;
  static const int PROJECT_SUBMISSION_ENTRY_DEFAULT_RETRY_COUNT = 0;
  static const int DEFAULT_SYNC_INTERVAL = 900000;
//  static String IMAGE_UUID_LONG_LAT_SEPARATOR = "##";

  static String DEFAULT_DELIMITER = "##";

  // onStartUp
  static String ON_STARTUP_DEFAULT = "DEFAULT";
  static String ON_STARTUP_ACTIVITY = "ACTIVITY";
  static String ON_STARTUP_SERVICE = "SERVICE";

  // DynamicForm Constants
  static String FORM_SHOULD_SHOW_TOOLBAR = "shouldShowToolbar";

  // OnStartup SharedPreferences
  static String ON_STARTUP_DATA_EXISTS = "ON_STARTUP_DATA_EXISTS";
  static String ON_STARTUP_DATA = "ON_STARTUP_DATA";
  static String ON_STARTUP_DATA_ARRAY = "ON_STARTUP_DATA_ARRAY";
  static String TOKEN_EXPIRY_MESSAGE = "Token is expired. Please login.";

  static String MOVE_TO_HOME = "MOVE_TO_HOME";

  // Tabular form Constants
  static String TABULAR_FORM_TABLE_FIELD = "table";
  static String TABULAR_FORM_NEW_PAGE_TABLE_FIELD = "new_page_table";
  static String TABULAR_FORM_TEXT_FIELD = "textview";
  static String TABULAR_FORM_RADIO_FIELD = "radio";
  static String TABULAR_FORM_EDITTEXT_FIELD = "edittext";
  static String TABULAR_FORM_SPINNER_FIELD = "dropdown";
  static String TABULAR_FORM_COMPUTED_TEXTVIEW = "computed_textview";
  static String TABULAR_FORM_GEOTAGGED_IMAGE_FUSED_FIELD = "geotagimagefused";
  static String TABULAR_FORM_ADDENDUM_FIELD = "addendum";
  static String TABULAR_FORM_DATE_FIELD = "date";
  static String TABULAR_FORM_CHECKBOX = "checkbox";

  static String VIEW_ALIGNMENT_LEFT = "left";
  static String VIEW_ALIGNMENT_CENTER = "center";
  static String VIEW_ALIGNMENT_RIGHT = "right";

  static String VIEW_LABEL_STYLE_BOLD = "bold";

  static String SUBMISSION_OBJECT_KEY = "key";
  static String SUBMISSION_OBJECT_DT = "dt";
  static String SUBMISSION_OBJECT_VAL = "val";

  static String IMAGE_PREVIEW_INTENT_EXTRA_IMAGE_PATH = "image_path";
  static String NO_DATA_AVAILABLE = "No data available!";

  static String APP_ID_TO_GROUPING_ATTRIBUTE_MAP_PREFERENCE_KEY =
      "APP_ID_TO_GROUPING_ATTRIBUTE_MAP_PREFERENCE_KEY";
  static String NO_PROJECTS_FOUND =
      "No results found. Please select appropriate filters.";
  static double NA_DOUBLE = -999;

  static String LATITUDE_KEY = "lat";
  static String LONGITUDE_KEY = "lon";

  static String MENU_FILTERING_ICON = "MENU_FILTERING_ICON";
  static String MENU_MAP_ICON = "MENU_MAP_ICON";
  static String FORGOT_PASSWORD = "FORGOT_PASSWORD";
  static String MENU_PROFILE_ICON = "MENU_PROFILE_ICON";
  static String SELECTED_FILTER_KEY_VALUE_MAP = "SELECTED_FILTER_KEY_VALUE_MAP";

  static String ROOT_CONFIG_APPLICATION_STATUS_INACTIVE = "inactive";
  static String ROOT_CONFIG_APPLICATION_STATUS_INACTIVE_MESSAGE =
      "This application is not active at the moment. Please check again later.";

  static String EXTERNAL_PROJECT_ID = "ext_proj_id";

  static String ICON_PATH = "/icons";
  static String DEFAULT_LABEL = "Default";
  static String SOURCE_KEY = "source";
  // Route name Constants
  static String loginRoute = '/login';
  static String homeRoute = '/home';
  static String getOTPRoute = '/getotp';
  static String changePasswordRoute = '/changepassword';
  static String projectGroupRoute = '/projectgroup';
  static String projectListRoute = '/projectlist';
  static String downloadsRoute = '/downloads';
  static String projectFormRoute = '/projectform';
  static String filterRoute = '/filter';
  static String userRegistrationRoute = '/register';
  static String geoTaggingRoute = '/geotagging';
}
