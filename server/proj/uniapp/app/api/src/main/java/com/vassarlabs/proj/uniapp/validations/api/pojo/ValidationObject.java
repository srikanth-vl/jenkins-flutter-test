package com.vassarlabs.proj.uniapp.validations.api.pojo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ValidationObject {
	
	@JsonProperty("expr")
	List<ExpressionValidationObject> exprValidationObjectList;
	
	@JsonProperty("api")
	List<APICallValidationObject> apiCallRequestObjectList;
	
	
	public ValidationObject() {
		this.exprValidationObjectList = new ArrayList<>();
		this.apiCallRequestObjectList = new ArrayList<>();
	}
}
