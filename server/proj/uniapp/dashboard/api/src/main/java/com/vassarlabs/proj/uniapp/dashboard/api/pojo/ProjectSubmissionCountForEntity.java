package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProjectSubmissionCountForEntity {
	
	@JsonProperty("parent_entity")
	String parentEntity;
	
	@JsonProperty("entity_name")
	String entityName;
	
	@JsonProperty("entity")
	String entity;
	
	@JsonProperty("last_month")
	LastNDaySubmissionCount last30DaysSubmissionCount;
	
	@JsonProperty("last_weak")
	LastNDaySubmissionCount last7DaysSubmissionCount;

	@JsonProperty("last_day")
	LastNDaySubmissionCount lastDaySubmissionCount;
}
