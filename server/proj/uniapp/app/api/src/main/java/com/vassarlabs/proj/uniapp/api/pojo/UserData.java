package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {

	@JsonProperty("super_app")
	UUID superAppId;

	@JsonProperty("user_id")
	String userId;

	@JsonProperty("user_details")
	String userDetails;

	@JsonProperty("dept_name")
	String departmentName;

	@JsonProperty("password")
	String password;

	@JsonProperty("app_actions")
	Map<UUID, String> appActions;

	@JsonProperty("insert_ts")
	long insertTs;

	@JsonProperty("mobile_number")
	long mobileNumber;

	@JsonProperty("is_active")
	boolean isActive;


}
