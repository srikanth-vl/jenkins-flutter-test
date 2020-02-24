package com.vassarlabs.proj.uniapp.constants;

import java.util.UUID;

public interface IFileUploadConstants {
	
	public static final String UPLOAD_TYPE = "upload_type";
	public static final String SUPERAPPID = "superAppId";
	public static final String APPID = "appId";
	public static final String TRUE = "true";
	public static final int DEFAULT_INTEGER_VALUE = 0;
	public static final String DEFAULT_STRING_VALUE = "";
	
	class UserProjectMappingUploadConstants {
		public static final String USER_ID = "userId";
		public static final String USER_TYPE = "userType";
		public static final String PROJECT_ID = "projectId";
		public static final String MAPPING = "mappings";
	}
	
	class UserFileUploadConfigConstants {
		public static final String USER_ID = "userId";
		public static final String USER_EXT_ID = "userExtId";
		public static final	String KEY = "key";
		public static final String VALUE = "value";
		public static final String PASSWORD = "password";
		public static final String USER_DETAILS =  "userDetails";
		public static final String APP_ACTIONS = "appActions";
		public static final String DEFAULT = "default";
		public static final String USER_NAME = "userName";
		public static final String MOBILE_NUMBER = "mobileNumber";
		public static final String EMAIL_ID = "emailId";
		public static final String DESIGNATION = "designation";
		public static final String ZONE = "zone";
		public static final String OTP_OBJECT = "otpObject";
		public static final String DEPT_NAME = "departmentName";
		public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
		
		public static final String DEFAULT_PASSWORD = "defaultPassword";
		public static final String HASHER = "bcrypt";
		public static final String DEF_PROVIDER = "userpass";
	}
	
	class MasterDataUploadConstants {
		public static final String KEYS = "keys";
		public static final String JSON_ATTRIBUTE_NAME = "attribute name";
		public static final String PROJECTID = "projectId";
		public static final String USER_ID = "userId";
		public static final String USER_TYPE = "userType";
		public static final String DATE = "date";
		public static final String USER_MAPPING = "userMapping";
		public static final String MAPPING = "mappings";
		public static final String MASTER_DATA_KEY_NAME = "masterDataName";
		public static final String KEY = "key";
		public static final String DEFAULT = "default";
		public static final String VALIDATIONS = "validations";
		public static final String EXPRESSION = "expr";
		public static final String ERROR_MESSAGE = "error_msg";
	}
	
	class FieldMetaDataUploadConstants {
		public static final String COLUMN_CONFIG = "columnConfig";
		public static final String PROJECT_ID = "projectId";
		public static final String KEYNAME = "key";
		public static final String KEYTYPE = "keyType";
		public static final String DEFAULT_KEYTYPE = "defaultKeyType";
		public static final String MASTER_DATA_KEY = "master";
		public static final String APP_DATA_KEY = "app";
		public static final String DEFAULT_DATA_KEY = "default";
		public static final String LABELNAME = "labelName";
		public static final String DATATYPE = "dataType";
		public static final String DEFAULT = "defaultValue";
		public static final String UOM = "uom";
		public static final String MANDATORY = "isMandatory";
		public static final String TARGET_FIELD = "target_field";
		public static final String DISPLAY_LABELS = "displayNames";
		public static final String VALIDATIONS = "validations";
		public static final String EXPRESSION = "expr";
		public static final String ERRORS = "error_msg";
		public static final String REQUIRED_COLUMNS = "requiredKeys";
		public static final String FORM_TYPE = "formType";
		public static final String UPDATE_MD_VERSION = "updateMdVersion";
		public static final String DEFAULT_PROJECT = "DEFAULT";
		public static final String FORM_JSON_CONFIG = "formJsonPath";
		public static final String EXTERNAL_KEY = "attributes";
		public static final String ALL_NAME = "ALL";
		
		public static UUID getAllProjectId() {
			return new UUID(1L, 1L);
		}
		
	}
	class EntityMetaDataUploadConstants {
		public static final String KEY = "key";
		public static final String PARENT = "parent";
		public static final String KEY_TYPE = "entityType";
	}
}
