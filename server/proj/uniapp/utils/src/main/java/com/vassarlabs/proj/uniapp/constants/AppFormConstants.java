package com.vassarlabs.proj.uniapp.constants;

import java.util.HashSet;
import java.util.Set;

public class AppFormConstants {
	
	public static final String USER_ID = "user_id";
	public static final String PROJECT_TYPE = "projecttype";
	public static final String DEPT_NAME = "deptname";
	public static final String CONTENT = "content";
	public static final String FORM_VERSION = "formversion";
	public static final String FORM_INSTANCE_ID = "forminstanceid";
	public static final String ACTIVE = "is_active";
	public static final String PROJECT_ID = "projectid";
	public static final String META_DATA_INSTANCE_ID = "mdinstanceid";
	
	public static final String KEY_SEPARATOR = "$$";
	public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";
	public static final String LABEL = "label";
	public static final String DATATYPE = "datatype";
	public static final String UOM = "uom";
	public static final String DEFAULT = "default";
	public static final String VALIDATIONS = "validations";
	public static final String MANDATORY_KEY = "mandatory";
	
	public static Set<String> getRequiredKeys() {
		Set<String> keys = new HashSet<>();
		keys.add(LABEL);
		keys.add(DATATYPE);
		keys.add(UOM);
		keys.add(DEFAULT);
		keys.add(VALIDATIONS);
		return keys;
	}

	public static Set<String> getRequiredKeysForHeaders() {
		Set<String> keys = new HashSet<>();
		keys.add(LABEL);
		return keys;
	}
	
}
