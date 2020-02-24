package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EntityMetaConfigInsertObject extends ApiRequestObject {

	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("config")
	List<EntityGroup> entityConfig;
	
	@JsonProperty("parent")
	String entityParent;
	
}
