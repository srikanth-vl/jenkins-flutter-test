package com.vassarlabs.proj.uniapp.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MapConfig {
	
	@JsonProperty("min_zoom")
	public Integer minZoom;

	@JsonProperty("max_zoom")
	public Integer maxZoom;
	
	@JsonProperty("offline_map_source_name")
	public String mapSourceName;
	
	@JsonProperty("map_base_url")
	public String mapBaseUrl;
	
}
