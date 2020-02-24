package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MediaMetaData {

	@JsonProperty("media_ext")
	String extension;
	
	@JsonProperty("sub_type")
	String subType;
	
	@JsonProperty("additional_prop")
	Map<String, Object> additionalProperties;
}
