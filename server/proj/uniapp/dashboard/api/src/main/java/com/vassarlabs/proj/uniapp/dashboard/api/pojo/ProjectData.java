package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectData {
	
	@JsonProperty("proj_name")
	String projectName;
	
	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("user_name")
	String userName;
	
	@JsonProperty("key_to_value_map")
	Map<String, String> keyToDataMap;
	
	@JsonProperty("media_paths")
	List<String> mediaPaths;
	
	@JsonProperty("date")
	int date;
	
	@JsonProperty("timestamp")
	long timestamp;
	
	@JsonProperty("user_type")
	String userType;
	
	@JsonProperty("project_id")
	UUID projectId;
	
	@JsonProperty("proj_ext_id")
	String projectExternalId;
}
