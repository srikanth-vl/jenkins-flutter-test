package com.vassarlabs.proj.uniapp.processor.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UniappFormDataList  {
	
	@JsonProperty("submit_data")
	List<AppFormData> appFormDataList;
	
	@JsonProperty("app_id")
	UUID appId;

	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("token")
	UUID tokenId;
	
	@JsonProperty("super_app")
	UUID superAppId;
}
