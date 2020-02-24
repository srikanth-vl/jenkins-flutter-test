package com.vassarlabs.proj.uniapp.app.insert.service.pojo;

import java.util.List;
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
public class FieldsDataObject{
	
	@JsonProperty("superapp")
	UUID superApp;
	
	@JsonProperty("appid")
	UUID appId;
	
	@JsonProperty("projectid")
	@Builder.Default
	UUID projectId = UUIDUtils.getDefaultUUID();
	
	@JsonProperty("form_type")
	String formType;
	
	@JsonProperty("version_update")
	boolean isVersionUpdate;
	
	@JsonProperty("default")
	List<FieldData> defaultFields;
	
	@JsonProperty("master")
	List<FieldData> masterFields;
	
	@JsonProperty("app")
	List<FieldData> appFields;
	
	@JsonProperty("proj_ext_id")
	@Builder.Default
	String projectExternalId = null;
}
