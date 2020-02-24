package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vassarlabs.prod.rest.call.object.RequestObject;
import com.vassarlabs.proj.uniapp.app.custom.deserialisers.CustomDeserialiser;

import lombok.Data;

@Data
public class AppJsonData {
	
	@JsonProperty("app")
	UUID appId;
	
	@JsonProperty("name")
	String appName;
	
	@JsonProperty("parent")
	UUID parentId;
	
	@JsonProperty("icon")
	String icon;
	
	@JsonProperty("desc")
	String desc;
	
	@JsonProperty("version")
	int version;
	
	@JsonProperty("alert_interval")
	int alertInterval;

	@JsonProperty("client_expiry_interval")
	int clientExpiry;
	
	@JsonProperty("formatter")
	FormatterObject formatter;
	
	@JsonProperty("external_api_list")
	Map<String, List<RequestObject>> externalAPIList;
	
	@JsonProperty("attributes")
	List<String> attributes;
	
	@JsonProperty("order")
	int order;
	
	@JsonProperty("sort_type")
	String sortType;
	
	@JsonProperty("grouping_attributes")
	List<String> groupingAttributes;
	
	@JsonProperty("filtering_form")
	JsonNode filteringForm;
	
	@JsonProperty("display_project_icon")
	Boolean displayProjectIcon;
	
}