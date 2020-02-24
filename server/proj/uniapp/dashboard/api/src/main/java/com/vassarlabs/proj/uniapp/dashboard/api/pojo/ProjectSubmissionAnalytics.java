package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProjectSubmissionAnalytics {
	
	@JsonProperty("super_app_id")
	UUID superAppId;
	
	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("project_submission_analyics")
	List<ProjectSubmissionCountForEntity> projectSubmissionCountForEntities;
}