package com.vassarlabs.proj.uniapp.utils.api.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vassarlabs.prod.rest.call.object.RequestObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIList {
	
	@JsonProperty("external_api_list")
	Map<String, List<RequestObject>> apiList;
}

