package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AppAnalyticsData {
	
	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("app_name")
	String appName;
	
	@JsonProperty("parent_id")
	UUID parentId;
	
	@JsonProperty("image_url")
	String imageUrl;
	
	@JsonProperty("total_submissions")
	int totalSubmissions;
	
	@JsonProperty("total_projects")
	int totalProjects;
	
	@JsonProperty("attribute_heirarchy")
	List<String> attribute_heirachy;
	
	@JsonProperty("assigned_projects")
	int assignedProjects;
}
