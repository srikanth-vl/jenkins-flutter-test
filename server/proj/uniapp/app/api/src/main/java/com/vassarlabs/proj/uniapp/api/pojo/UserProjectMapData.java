package com.vassarlabs.proj.uniapp.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectMapData {

	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("user_priority")
	UserPriorities userType;
	
}