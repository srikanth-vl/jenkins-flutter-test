package com.vassarlabs.proj.uniapp.constants;

import java.util.ArrayList;
import java.util.List;

public interface CommonConstants {

	public static final String DATATYPE_INTEGER = "integer";
	public static final String DATATYPE_DOUBLE = "double";
	public static final String DATATYPE_STRING = "string";
	public static final String DATATYPE_LONG = "long";
	public static final String DATATYPE_VIDEO = "video";

	
	public static final int TOKEN_EXPIRED = 1;
	public static final int TOKEN_NOT_EXPIRED = 0;
	
	public static final String REQUEST_TYPE_GET = "GET";
	public static final String REQUEST_TYPE_POST = "POST";
	
	public static final int DEFAULT_DATE = 00000000;
	public static final String DATATYPE_DATE = "date";
	public static final String DATATYPE_IMAGE = "image";
	public static final String DATATYPE_GEOTAG = "geotag";
	public static final String DATATYPE_TIME = "time";
	public static final String DATATYPE_TIMESTAMP = "timestamp";
	public static final String DATATYPE_FORMATTER = "datatype_formatter";
	public static final String ATTRIBUTES = "attributes";
	public static final String DATATYPE_BBOX = "bbox";
	public static final String DATATYPE_JSON_ARRAY = "json_array";
	public static final String DATATYPE_CENTER_RADIUS_ENVELOPE = "circle_validation";
	public static final String DATATYPE_JSON = "json";
	
	public static final String CURRENT_DATE_STR = "current";
	public static final String META_DATA_INSTANCE_ID = "meta_data_instance_id";
	public static final String DELIMITER = "##";
	public static final String FORM_SUBMIT_TIMESTAMP = "form_submit_ts";
	public static final String FORM_SUBMIT_TIME = "form_submit_time";
	
	public static final int NAInteger = -999;
	public static final String SUPER_APP_ID = "superapp";
	public static final String APP_ID = "app";
	public static final String PROJECT_ID = "projectid";
	public static final String FORM_TYPE = "formtype";
	public static final String META_DATA_VERSION = "mdversion";
	public static final String DATE = "date";
	public static final String ACTIONS = "actions";
	public static final int MAX_LIST_SIZE = 10000;
	
	public static final Integer MAX_BATCH_SIZE = 10000;
	public static final String JSON_CONFIG = "JSON_CONFIG";
	
	public static final String DELETE_INSERT_UPLOAD = "delete_insert";
	public static final String INSERT_UPLOAD = "insert";
	public static final String DEFAULT_UPLOAD_TYPE = INSERT_UPLOAD;
	public static final String USER_PROJ_MAPPING_CONFIG = "USER_PROJ_CONFIG";
	public static final String MASTER_DATA = "master_data";
	public static final String USER_PROJECT_MAPPING = "user_project_mapping";
	public static final String USER_DATA = "user_data";
	
	public static final String USER_DATA_CLASSNAME = "UserDataInfo";
	public static final String MASTER_DATA_CLASSNAME = "MasterDataInfo";
	public static final String MASTER_DATA_CLASSNAME_GENERIC = "Genereic_project_data";
	public static final String USER_MAPPING_CLASSNAME = "UserMappingInfo";
	public static final String FIELDMETADATA_CLASSNAME = "FieldDataInfo";
	
	public static final String ENTITY_META_DATA_CONFIG_CLASSNAME = "entityMetaDataConfigInfo";
	public static final String RESET_PASSWORD_OTP_MESSAGE = " is your OTP for password reset. This OTP is valid till ";
	public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{6,}$";
	public static final String MOBILE_NIMBER_PATTERN = "^[6789]\\d{9}$";
	
	public static final String DEFAULT_USER_ID = "00000";
	public static final String LAST_SYNC_USER_ID = "last_sync_user_id";
	public static final String KEY_DELIMITER = "$$";
	public static final String KEY_DELIMITER_REGEX = "\\$\\$";
	public static final String DEFAULT_DATE_FORMAT = "dd/mm/yyy";
	public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
	public static final String DEFAULT_TIMESTAMP_FORMAT = "dd/mm/yyy HH:mm:ss";
	public static final String DEFAULT_DOUBLE_FORMAT = "#.##";
	public static final long DEFAULT_LONG_VALUE = 0 ;
	public static final int DEFAULT_INT_VALUE = 0 ;
	public static final int MAX_RETRIES = 3;
	public static final String MANDATORY_VALIDATION_KEY = "mandatory_validation";
	public static final String APP_DATA_LIST = "keysFromApp";
	
	public static final String APP_CONFIG_DATA_KEY_NAME = "name";
	public static final String EMPTY_STRING = "";
	public static final String LAST_24_HRS_DATA = "Last 24 hrs data";
	public static final String LAST_7_DAYS_DATA = "Last 7 days data";
	public static final String NA = "N/A";
	public static final String MAIL_SUBJECT_FOR_USAGE_DATA = "Usage Report Data";
	public static final String MAIL_BODY_FOR_USAGE_DATA = "Hi, \n Please find attached the usage report data below.";
	public static final String USER_ID = "User Id";
	public static final String USER_MOBILE_NO = "Mobile Number";
	public static final String USER_DETAILS = "User Details";
	
	public static final String FORM_SUBMIT_RECEIVER_CLASS = "form-submit-receiver-class";
	public static final String MEDIA_SUBMIT_RECEIVER_CLASS = "media-submit-receiver-class";
	
	public static final Boolean STATUS_TRUE = true;
	public static final String MEDIA = "media";
	public static final long THREAD_SLEEP_TIME = 200;
	public static final String DEFAULT_KEY = "DEFAULT";
	public static final String GPS_VALIDATION_KEY = "GPS_KEY";
	public static final String IMAGE_ORIENTATION_KEY = "orientation";
	public static final Integer MAX_RELAY_RETRIES = 7;
	public static final String PROJECT_SUBMISSION_ANALYTICS_DEFAULT_HIERARCHY_ELEMENT= "ALL";
	public static final String PROJECT_SUBMISSION_ANALYTICS_ALL_PROJECTS= "All Projects";
	public static List<String> getAllUploadTypes() {
		List<String> uploadTypes = new ArrayList<>();
		uploadTypes.add(DELETE_INSERT_UPLOAD);
		uploadTypes.add(INSERT_UPLOAD);
		return uploadTypes;
	}
	
	public static List<String> getAllDataTypes() {
		List<String> dataTypes = new ArrayList<>();
		dataTypes.add(DATATYPE_DATE);
		dataTypes.add(DATATYPE_DOUBLE);
		dataTypes.add(DATATYPE_IMAGE);
		dataTypes.add(DATATYPE_TIME);
		dataTypes.add(DATATYPE_TIMESTAMP);
		dataTypes.add(DATATYPE_INTEGER);
		dataTypes.add(DATATYPE_LONG);
		dataTypes.add(DATATYPE_STRING);
		dataTypes.add(DATATYPE_GEOTAG);
		dataTypes.add(DATATYPE_VIDEO);
		dataTypes.add(DATATYPE_BBOX);
		dataTypes.add(DATATYPE_CENTER_RADIUS_ENVELOPE);
		dataTypes.add(DATATYPE_JSON_ARRAY);
		return dataTypes;
	}
	
}
