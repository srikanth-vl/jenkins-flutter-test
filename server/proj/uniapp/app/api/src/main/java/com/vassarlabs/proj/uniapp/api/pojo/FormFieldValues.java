package com.vassarlabs.proj.uniapp.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vassarlabs.proj.uniapp.app.custom.deserialisers.CustomDeserialiser;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FormFieldValues{
	
	@JsonProperty("key")
	String key;
	
	@JsonProperty("val")
	@JsonDeserialize(using = CustomDeserialiser.class)
	String value;
	
	@JsonProperty("dt")
	String dataType;
	
	@JsonIgnoreProperties("ui")
	String ui;
	
	@JsonProperty("uom")
	String uom;
	
}
