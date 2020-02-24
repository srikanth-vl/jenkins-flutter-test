package com.vassarlabs.proj.uniapp.constants;

import java.util.HashMap;
import java.util.Map;

public class ProjectListConstants {

	public static final String PROJECT_ID = "projectid";
	public static final String PROJECT_NAME = "projectname";
	public static final String PROJECT_ICON = "project_icon";
	public static final String STATE = "state";
	public static final String LABEL_NAME = "labelname";
	public static final String PRIMARY = "primary";
	public static final String SECONDARY = "secondary";
	public static final String USER_ID = "userid";
	public static final String FIELDS = "fields";
	public static final String IDENTIFIER = "key";
	public static final String VALUE = "value";
	public static final String VALIDATIONS = "validations";
	public static final String LAST_SUBMISSION_DATE = "last_sub_date";
	public static final String ATTRIBUTES = "attributes";
	
	public static final String NA_STRING = "N/A";
	public static final String GEO_TAG_LAT = "lat";
	public static final String GEO_TAG_LONG = "long";
	public static final String EMPTY_STRING = "";
	public static final String PRIORITY = "priority";
	public static final String STATE_KEY = "proj_state";
	public static final String LAST_SYNC_TS = "last_sync_ts";
	public static final String EXTERNAL_PROJECT_ID = "ext_proj_id";
	private static final String GEOTAG = "geotag"; 
	
	public static final String SHOW_MAP = "showmap";
	public static final String TYPES = "types";
	public static final String USER_TYPE = "user_type";
	public static final String PROJECTS = "projects";
	public static final String TIME = "time";
	public static final String DATE = "date";
	public static final String UOM = "uom";

	public static final String DB_DATE_FORMAT = "yyyyMMdd";
	
	public static final Map<String, String> MASTER_TO_PROJECT_CONSTANT_MAP;
	
	static {
		MASTER_TO_PROJECT_CONSTANT_MAP = new HashMap<String, String>();
		MASTER_TO_PROJECT_CONSTANT_MAP.put(MasterDataKeyNames.PROJ_NAME_KEY, PROJECT_NAME);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(MasterDataKeyNames.PROJECT_ICON_KEY, PROJECT_ICON);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(MasterDataKeyNames.STATE_KEY, STATE);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(MasterDataKeyNames.LAST_SYNC_TS, LAST_SYNC_TS);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(MasterDataKeyNames.EXTERNAL_PROJECT_ID, EXTERNAL_PROJECT_ID);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(MasterDataKeyNames.PRIORITY, PRIORITY);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(MasterDataKeyNames.GEO_TAG_KEY, GEOTAG);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(MasterDataKeyNames.LAST_SUBMISSION_DATE, LAST_SUBMISSION_DATE);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(CommonConstants.DATATYPE_BBOX, CommonConstants.DATATYPE_BBOX);
		MASTER_TO_PROJECT_CONSTANT_MAP.put(CommonConstants.DATATYPE_CENTER_RADIUS_ENVELOPE, CommonConstants.DATATYPE_CENTER_RADIUS_ENVELOPE);
	}

}
