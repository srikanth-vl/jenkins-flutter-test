package com.vassarlabs.proj.uniapp.validations.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FormValidationObject 
	extends ValidationObject {
	
	@JsonProperty("mandatory")
	boolean isMandatory;
	}
