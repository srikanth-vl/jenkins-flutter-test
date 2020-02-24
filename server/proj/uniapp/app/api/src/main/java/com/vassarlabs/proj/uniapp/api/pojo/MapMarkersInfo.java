package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MapMarkersInfo {
	@JsonProperty("map_entity_name")
    private String mapEntityName;
	
    @JsonProperty("map_entity_additional_info")
    private Map<String, String> additionalInfo;
    
    @JsonProperty("download_url")
    private String downloadUrl;
    
    @JsonProperty("has_toggle")
    private boolean hasToggle;
    
    @JsonProperty("icon_url")
    private String iconUrl;

}
