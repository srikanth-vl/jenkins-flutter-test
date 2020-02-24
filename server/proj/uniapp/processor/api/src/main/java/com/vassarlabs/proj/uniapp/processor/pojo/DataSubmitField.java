package com.vassarlabs.proj.uniapp.processor.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSubmitField {

	@JsonProperty("value")
	String value;
	
	@JsonProperty("timestamp")
	Long timestamp;
}