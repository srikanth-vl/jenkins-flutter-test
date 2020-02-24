package controllers.com.vassarlabs.play.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

import controllers.com.vassarlabs.play.constants.ResponseConstants;
import models.com.vassarlabs.play.models.ApiResponse;
import play.mvc.Controller;
import play.mvc.Result;
import services.com.vassarlabs.play.services.ApiResponseFactory;
import views.html.index;


@org.springframework.stereotype.Controller
public class BasicController 
	extends Controller {

	public Result preflight(String any) {
		System.out.println("any in BasicController.preflight: " + any);
		response().setHeader("Access-Control-Allow-Origin", "*");
		response().setHeader("Allow", "*");
		response().setHeader("Access-Control-Allow-Methods",
				"POST, GET, PUT, DELETE, OPTIONS");
		response()
				.setHeader(
						"Access-Control-Allow-Headers",
						"Origin, X-Requested-With, Content-Type, Accept, Referer, User-Agent, Authorization, Authentication");
    
		return ok();
	}

	public Result index(String any) {
		return ok(index.render());
	}
    
	public boolean validateInput(Class<?> dataType, String value) {
		if(dataType.equals(Integer.class)
				&& value.matches("\\d+")) {
				return true;
		} else if(dataType.equals(UUID.class)
				&& value.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
				return true;
		} else if(dataType.equals(Long.class) 
				&& value.matches("\\d{10}")) {
				return true;
		}
		return false;
	}
	
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
	
	public ApiResponse createResponse(ServiceOutputObject serviceObject, ApiResponse response) {

		if (serviceObject != null) {
			Map<String, Object> jsonData = serviceObject.getOutputMap();
			response = ApiResponseFactory.createResponse(jsonData);
		} else {
			response.setResult(false, ResponseConstants.SERVICE_OUTPUT_NULL_CODE, null, ResponseConstants.SERVICE_OUTPUT_NULL_MESSAGE);
		}
		return response;
	}
	
	public ApiResponse createResponse(List<?> outputDataList, ApiResponse response) {

		if (outputDataList != null && !outputDataList.isEmpty()) {
			response = ApiResponseFactory.createResponse(outputDataList);
		} else {
			response.setResult(false, ResponseConstants.SERVICE_OUTPUT_NULL_CODE, null, ResponseConstants.SERVICE_OUTPUT_NULL_MESSAGE);
		}
		return response;
	}
	
	
	public double getDoubleValue(Map<String, String[]> requestMap, ApiResponse response, String parameter) {

		double value = 0;
		if (requestMap.containsKey(parameter)) {
			value =  Double.parseDouble(requestMap.get(parameter)[0]);
		}
		return value;
	}

}