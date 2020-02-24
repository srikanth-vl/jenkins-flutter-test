package com.vassarlabs.proj.uniapp.fileupload.object;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Validations {
	@JsonProperty("expr")
	String expr;
	@JsonProperty("error_msg")
	String errorMessage;
}
