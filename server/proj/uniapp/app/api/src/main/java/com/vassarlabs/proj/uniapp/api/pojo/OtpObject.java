package com.vassarlabs.proj.uniapp.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class OtpObject {
	@JsonProperty("otp")
	int otp;
	
	@JsonProperty("timestamp")
	long timestamp;
	
}
