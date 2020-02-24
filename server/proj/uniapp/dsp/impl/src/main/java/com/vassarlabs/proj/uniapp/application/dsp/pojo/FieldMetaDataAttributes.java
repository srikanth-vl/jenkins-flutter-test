package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldMetaDataAttributes {

	@JsonProperty("external_key")
	String externalKey;
	
	@JsonProperty("field_attributes")
	Map<String, String> attributes;
	
}