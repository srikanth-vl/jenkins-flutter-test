package com.vassarlabs.proj.uniapp.upload.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProjectUsersData {
	
	@JsonProperty("user_list")
	List<String> userIdList;
	
	@JsonProperty("user_type")
	String userType;
	
	@JsonProperty("proj_ext_id")
	String externalProjectId;
}
