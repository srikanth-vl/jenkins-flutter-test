package com.vassarlabs.proj.uniapp.enums;

import java.util.HashMap;
import java.util.Map;

public enum APITypes {

	LOGIN("login"),
	ROOT_CONFIG("root_config"),
	PROJ_TYPE_CONFIG("project_type_config"),
	PROJECT_LIST_WITH_TS("project_list_ts"),
	PROJECT_LIST_CONFIG("project_list_config"),
	SUBMIT("form_submit"), 
	FORM_MEDIA_SUBMIT("form_media_submit"), 
	INSERT("insert"),
	CHANGE_PASSWORD("change_password"),
	LOGOUT("logout"),
	TRANSACTION_LOG("fetch_transaction_log"),
	RESET_PASSWORD("reset_password"),
	GENERATE_OTP("generate_otp"),
	MEDIA_UPLOAD("image_geotag_insert"), 
	FORM_MEDIA_RELAY("form_media_relay"),
	FORM_TEXT_DATA_RELAY("form_text_data_relay"),
	SEND_TO_QUEUE("send_to_queue"), 
	ENTITY_META_DATA_CONFIG("entity_meta_data_config"),
	MEDIA_DOWNLOAD("media_download"),
	LOCALIZATION_CONFIG("localization_config"),
	MAP_CONFIG("localization_config"),
	USER_PASSWORD_CHANGE("user_password_change");


	private static final Map<String, APITypes> apiIdToAPINameMap = new HashMap<String, APITypes>();
	private final String value;

	static {
		for (APITypes myEnum : values()) {
			apiIdToAPINameMap.put(myEnum.getValue(), myEnum);
		}
	}
	APITypes(final String newValue) {
		value = newValue;
	}

	public String getValue() { 
		return value; 
	}	

	public static APITypes getAPINameByValue(String value) {
		return apiIdToAPINameMap.get(value);
	}
}
