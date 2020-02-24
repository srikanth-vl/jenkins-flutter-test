package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LastNDaySubmissionCount {
	
	@JsonProperty("successful_project_submission_count")
	int successfulSubmissionCount;
	
	@JsonProperty("successful_projects")
	Set<UUID> successfulProjectIds;
	
	@JsonProperty("failed_project_submission_count")
	int failedSubmissionCount;
	
	@JsonProperty("failed_projects")
	Set<UUID> failedProjectids;

}
