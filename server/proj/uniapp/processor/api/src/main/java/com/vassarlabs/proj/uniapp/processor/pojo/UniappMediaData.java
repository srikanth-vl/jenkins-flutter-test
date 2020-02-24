package com.vassarlabs.proj.uniapp.processor.pojo;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UniappMediaData {
	
	@JsonProperty("fld_id")
	UUID fieldUUID;
	
	@JsonProperty("proj_id")
	UUID projectId;
	
	@JsonProperty("lat")
	double latitude;
	
	@JsonProperty("long")
	double longitude;
	
	@JsonProperty("sync_ts")
	long syncTimeStamp;
		
	@JsonProperty("image")
	byte[] mediaContent;
	
	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("token")
	UUID tokenId;
	
	@JsonProperty("super_app")
	UUID superAppId;
	
	@JsonProperty("proj_ext_id")
	String projectExternalId;
	
	@JsonProperty("media_path")
	String mediaPath;
	
	@JsonProperty("media_type")
	String mediaType;

	@JsonProperty("media_subtype")
	String mediaSubtype;
	
	@JsonProperty("media_ext")
	String mediaFileExtension;
	
	@JsonProperty(value = "insert_ts")
	long insertTs;
	
	@JsonProperty("gps_accuracy")
	String gpsAccuracy;

	@JsonProperty("timestamp_overlay")
	String timestampOverlay;
	
	@JsonProperty("additional_props")
	Map<String, String> otherParams;
	
	@JsonProperty("proj_id_ver")
	Map<UUID, Map<String, Integer>> projectIdToFormTypeToVersionMap;
}
