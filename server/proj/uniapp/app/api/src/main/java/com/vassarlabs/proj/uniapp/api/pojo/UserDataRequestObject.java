package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataRequestObject {
	
	@JsonProperty("super_app")
	UUID superAppId;
	
	@JsonProperty("user_ids")
	List<String> userIdList;
	
}