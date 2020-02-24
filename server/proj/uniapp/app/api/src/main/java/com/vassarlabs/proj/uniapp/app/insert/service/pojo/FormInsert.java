package com.vassarlabs.proj.uniapp.app.insert.service.pojo;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vassarlabs.prod.common.utils.UUIDUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormInsert {
	
	@JsonProperty
	UUID superAppId;
	
	@JsonProperty
	UUID appId;
	
	@JsonProperty
	String formType;
	
	@JsonProperty("projectid")
	@Builder.Default
	UUID projectId = UUIDUtils.getDefaultUUID();
	
	@JsonProperty
	String metadataInstanceId;
	
	@JsonProperty
	int isActive;
	
	@JsonProperty
	String formJson;
	
	@JsonProperty("proj_ext_id")
	String projectExternalId;
}
