package com.vassarlabs.proj.uniapp.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ProjectIconInfo {
	@JsonProperty("static_url")
	String staticUrl;
	@JsonProperty("dynamic_key_name")
	String dynamicKeyName;
}
