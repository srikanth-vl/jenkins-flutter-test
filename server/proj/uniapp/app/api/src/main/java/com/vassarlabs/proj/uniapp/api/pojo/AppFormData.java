package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vassarlabs.proj.uniapp.app.custom.deserialisers.CustomDeserialiser;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AppFormData {
	
	@JsonProperty("form_id")
	String formInstanceId;
	
	@JsonProperty("md_instance_id")
	String metaDataInstanceId;
	
	@JsonProperty("proj_id")
	UUID projectId;
	
	@JsonDeserialize(using = CustomDeserialiser.class)
	@JsonProperty("user_type")
	String userType;
	
	@JsonProperty("insert_ts")
	long timeStamp;
	
	@JsonProperty("fields")
	List<FormFieldValues> formFieldValuesList;	
	
	@JsonProperty("additional_props")
	Map<String, String> otherParams;
}
