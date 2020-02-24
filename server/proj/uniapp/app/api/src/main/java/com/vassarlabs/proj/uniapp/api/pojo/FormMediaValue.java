package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FormMediaValue
	extends AppFormRequestObject {
	
	@JsonProperty("fld_id")
	UUID mediaUUID;
	
	@JsonProperty("proj_id")
	UUID projectId;
	
	@JsonProperty("media_type")
	String mediaType;

	@JsonProperty("media_subtype")
	String mediaSubtype;
	
	@JsonProperty("media_ext")
	String mediaFileExtension;
	
	@JsonProperty("image")
	byte[] mediaContent;

	@JsonProperty("lat")
	Double latitude;
	
	@JsonProperty("long")
	Double longitude;
	
	@JsonProperty("gps_accuracy")
	String gpsAccuracy;

	@JsonProperty("timestamp_overlay")
	String timestampOverlay;
	
	@JsonProperty("additional_props")
	Map<String, String> otherParams;

	@JsonProperty("sync_ts")
	long syncTimeStamp;
	
	@JsonProperty("insert_ts")
	long insTimeStamp;	
	
	@JsonProperty("media_path")
	String mediaPath;
	
}
