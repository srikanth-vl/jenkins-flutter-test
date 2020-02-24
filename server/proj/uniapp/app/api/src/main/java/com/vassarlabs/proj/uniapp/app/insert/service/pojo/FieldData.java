package com.vassarlabs.proj.uniapp.app.insert.service.pojo;

import org.springframework.data.cassandra.core.mapping.Column;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vassarlabs.proj.uniapp.app.custom.deserialisers.CustomDeserialiser;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationObject;

import lombok.Data;

@Data
public class FieldData {
	
	@JsonProperty("key")
	String key;
	
	@JsonProperty("label")
	String label;
	
	@JsonProperty("datatype")
	String dataType;
	
	@JsonProperty("default")
	String defValue;
	
	@JsonProperty("uom")
	String uom;
	
	@JsonProperty("mandatory")
	boolean isMandatory;
	
	@JsonProperty("target_key")
	String targetKey;
	
	@JsonProperty("display_labels")
	String displayLabels;
	
	@JsonProperty("validations")
	ValidationObject validations;
	
	@JsonProperty("computation_type")
	String computationType;
	
	@JsonProperty("dimension")
	String dimension;
	
	@JsonDeserialize(using = CustomDeserialiser.class)
	@JsonProperty("attributes")
	String attributes;
	
}
