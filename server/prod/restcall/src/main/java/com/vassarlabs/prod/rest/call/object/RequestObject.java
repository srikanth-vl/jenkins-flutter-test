package com.vassarlabs.prod.rest.call.object;

import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RequestObject {
	public static final String GET_REQUEST = "GET";
	public static final String POST_REQUEST = "POST";
	@JsonProperty("request_url")
	String url;
	
	@JsonProperty("request_type")
	String requestType;
	
	@JsonProperty("request_params")
	Map<String, Object> params;
		
	@JsonIgnore
	Map<String, Object> headers;
	
	@JsonIgnore
	MultiValueMap<String, Object> multiValueParams;
}
