package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class ServiceOutputObject
	implements IServiceOutputObject {
	
	public ServiceOutputObject(Map<String, Object> outputMap, UserTrackingObject trackingObject, boolean isSuccessful) {
		this.outputMap = outputMap;
		this.trackingObject = trackingObject;
		this.isSuccessful = isSuccessful;
	}
	
	Map<String, Object> outputMap;
	UserTrackingObject trackingObject;
	boolean isSuccessful;
	Map<String, List<String>> keyToErrorMessages = new HashMap<>(); // For validation failure messages
}
