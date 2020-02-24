package com.vassarlabs.proj.uniapp.upload.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserProjectMappingInput {
	
	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("token")
	UUID tokenId;
	
	@JsonProperty("super_app")
	UUID superAppId;
	
	@JsonProperty("user_project_mapping")
	List<ProjectUsersData> projectUserMappingInput;
}
