package com.vassarlabs.proj.uniapp.validations.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data	
public class APICallValidationObject {

	@JsonProperty("route")
	String route;
	
	@JsonProperty("req_type")
	String requestType;
	
	@JsonProperty("params")
	String params;
	
	@JsonProperty("error_msg")
	String errorMessage;
}
