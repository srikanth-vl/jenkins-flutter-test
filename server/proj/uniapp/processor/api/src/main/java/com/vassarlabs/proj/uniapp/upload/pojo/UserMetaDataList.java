package com.vassarlabs.proj.uniapp.upload.pojo;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserMetaDataList {
	
	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("user_ext_id")
	String userExternalId;
	
	@JsonProperty("dept_name")
	String department;
	
	@JsonProperty("password")
	String password;
	
	@JsonProperty("app_actions")
	Map<UUID, String> appActions;
	
	@JsonProperty("mobile_number")
	long mobileNumber;
	
	@JsonProperty("name")
	String userName;
	
	@JsonProperty("map_urls")
	String mapUrlsString;
	
	@JsonProperty("designation")
	String designation;
	
	@JsonProperty("email")
	String emailId;
	
	@JsonProperty("zone")
	String zone;
	
	@JsonProperty("addition_properties")
	Map<String, String> additionalProperties;
}