package com.vassarlabs.proj.uniapp.validations.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionValidationObject {
	
	@JsonProperty("expr")
	String expression;
	
	@JsonProperty("error_msg")
	String errorMessage;
}
