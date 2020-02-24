package com.vassarlabs.proj.uniapp.processor.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AppFormData {
	
	@JsonProperty("form_id")
	String formInstanceId;
	
	@JsonProperty("md_instance_id")
	String metaDataInstanceId;
	
	@JsonProperty("proj_id")
	UUID projectId;
	
	@JsonProperty("user_type")
	String userType;
	
	@JsonProperty("insert_ts")
	long timeStamp;
	
	@JsonProperty("fields")
	List<FormFieldValues> formFieldValuesList;	
}
