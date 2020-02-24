package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class ProjectListRequestObject
	extends ApiRequestObject {
	
	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("md_instance_id")
	List<String> metadataInstanceList;
	
	@JsonProperty("proj_instance_key")
	String projectInstanceKey;
	
	@JsonProperty("proj_ids")
	List<UUID> projectIdList;
}
