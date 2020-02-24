package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BusinessAnalyticsResponse {
	
	@JsonProperty("parent_entity")
	String parentEntity;
	
	@JsonProperty("entity")
	String entity;
	
	@JsonProperty("fields")
	List<String> fields;
	
	@JsonProperty("labels")
	Map<String, String> labels;
	
	@JsonProperty("computation_type")
	Map<String, String> computationType;
	
	@JsonProperty("uom")
	Map<String, String> uom;
	
	@JsonProperty("entity_name")
	String entityName;
	
	@JsonProperty("computed_values")
	Map<String, Object> computedValues;

	@JsonProperty("insert_ts")
	long insertTs;
	
}
