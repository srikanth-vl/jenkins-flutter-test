package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MediaDownloadRequestParams
	extends AppFormRequestObject {
	
	@JsonProperty("proj_id")
	UUID projectId;
	
	@JsonProperty("media_type")
	String mediaType;
	
	@JsonProperty("fld_id")
	UUID mediaUUID;
}
