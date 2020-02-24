package services.com.vassarlabs.play.services;

import java.util.Map;
import java.util.UUID;

import com.vassarlabs.prod.common.utils.UUIDUtils;

import models.com.vassarlabs.play.models.ApiResponse;

public class CommonUtils {
	
	public UUID getUUIDValue(Map<String, String[]> requestMap, ApiResponse response, String parameter) {
		UUID uuid = null;
		if (requestMap.containsKey(parameter)) {
			uuid =  UUIDUtils.toUUID(requestMap.get(parameter)[0]);
		} 
		return uuid;
	}

	public Long getLongValue(Map<String, String[]> requestMap, ApiResponse response, String parameter) {

		Long value = null;
		if (requestMap.containsKey(parameter)) {
			value =  Long.parseLong(requestMap.get(parameter)[0]);
		}
		return value;
	}
	
	public String getStringValue(Map<String, String[]> requestMap, ApiResponse response, String parameter) {

		String value = null;
		if (requestMap.containsKey(parameter)) {
			value =  requestMap.get(parameter)[0];
		}
		return value;
	}
	
	public double getDoubleValue(Map<String, String[]> requestMap, ApiResponse response, String parameter) {

		double value = 0;
		if (requestMap.containsKey(parameter)) {
			value =  Double.parseDouble(requestMap.get(parameter)[0]);
		}
		return value;
	}

}
