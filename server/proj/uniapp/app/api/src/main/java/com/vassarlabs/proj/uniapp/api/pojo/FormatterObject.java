package com.vassarlabs.proj.uniapp.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FormatterObject {
	
	@JsonProperty("double")
	String doubleVal;
	
	@JsonProperty("date")
	String date;
	
	@JsonProperty("time")
	String time;
	
	@JsonProperty("timestamp")
	String timestamp;
	
	@JsonProperty("string")
	String stringVal;

}
