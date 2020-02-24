package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProjectSubmissionData {
	@JsonProperty("project_id")
	UUID projectId;
	
	@JsonProperty("project_name")
	String projectName;
	
	@JsonProperty("time")
	String time;
	
	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("date")
	String date;
	
	@JsonProperty("submitted_data")
	Map<String, String> submittedData;
	
}
