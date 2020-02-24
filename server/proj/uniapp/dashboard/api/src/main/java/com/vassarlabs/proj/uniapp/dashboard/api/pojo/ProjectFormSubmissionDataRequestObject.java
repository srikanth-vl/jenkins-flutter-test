package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(callSuper = true)
public class ProjectFormSubmissionDataRequestObject {
	@JsonProperty("super_app_id")
	UUID superAppId;
	
	@JsonProperty("app_id")
	UUID appId;
	@JsonProperty("project_ids")
	List<UUID> projectIds;
}
