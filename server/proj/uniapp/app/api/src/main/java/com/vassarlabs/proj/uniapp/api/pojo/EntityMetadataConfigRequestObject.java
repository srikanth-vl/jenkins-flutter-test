package com.vassarlabs.proj.uniapp.api.pojo;

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
public class EntityMetadataConfigRequestObject
	extends ApiRequestObject {
	
	@JsonProperty("app_id")
	UUID appId;

	@JsonProperty("project_id")
	UUID projectId;
	

	@JsonProperty("parent_entity")
	String parentEntity;
	
	@JsonProperty("entity_meta_data_user_id")
	String entityMetadtaUserId;

	@JsonProperty("entity_name")
	String entityName;
	

	@JsonProperty("last_sync_ts")
	long lastSyncTs;
	
}
