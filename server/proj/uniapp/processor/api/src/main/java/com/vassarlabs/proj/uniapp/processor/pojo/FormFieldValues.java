package com.vassarlabs.proj.uniapp.processor.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FormFieldValues{
	
	@JsonProperty("key")
	String key;
	
	@JsonProperty("val")
	String value;
	
	@JsonProperty("dt")
	String dataType;
	
	@JsonIgnoreProperties("ui")
	String ui;

	@JsonProperty("uom")
	String uom;
	
}
