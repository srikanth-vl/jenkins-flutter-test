package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LatestProjDataRequestObject 
	extends AppRequestObject{
	
	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("project_ids")
	List<String> projectIds;
}
