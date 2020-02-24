package com.vassarlabs.proj.uniapp.fileupload.object;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KeyObject {
	
	@JsonProperty("key")
	String key;
	
	@JsonProperty("default")
	String defaultValue;
}
