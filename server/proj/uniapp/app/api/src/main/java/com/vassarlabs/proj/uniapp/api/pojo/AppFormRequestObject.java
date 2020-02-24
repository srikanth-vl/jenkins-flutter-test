package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
@ToString
public class AppFormRequestObject 
	extends ApiRequestObject {
	
	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("proj_id_ver")
	Map<UUID, Map<String, Integer>> projectIdToFormTypeToVersionMap;
}
