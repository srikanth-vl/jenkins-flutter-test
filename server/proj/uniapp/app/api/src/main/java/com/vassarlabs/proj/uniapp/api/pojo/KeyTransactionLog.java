package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class KeyTransactionLog
	extends ApiRequestObject {
	
	@JsonProperty("app_id")
	UUID appId;
	
	@JsonProperty("project_id")
	UUID projectId;
	
	@JsonProperty("key")
	String key;
}
