package controllers.com.vassarlabs.play.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.processor.pojo.UniappFormDataList;
import com.vassarlabs.proj.uniapp.processor.pojo.UniappMediaData;
import com.vassarlabs.proj.uniapp.processor.service.IDataProcessorService;

import controllers.com.vassarlabs.play.constants.FieldConstants;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

@Controller
public class RESTServiceRecieverController 
	extends BasicController {

	@Autowired IDataProcessorService dataProcessingService;
	
	
	public Result processTextData() {
		
		JsonNode jsonNode = request().body().asJson();
		String formDataJson =  jsonNode.findPath("data").toString();
		System.out.println("In processTextData()- " + formDataJson);
		ObjectMapper objectMapper = new ObjectMapper();
		
		UniappFormDataList dataList;
		try {
			dataList = objectMapper.readValue(formDataJson, UniappFormDataList.class);
			boolean result = dataProcessingService.processTextData(dataList);
			return ok(Json.toJson(result));  
		} catch (IOException e) {
			Logger.error("In processTextData()- IO Exception");
			e.printStackTrace();
		}
		return ok(Json.toJson(false));
	}
	
	public Result processMediaData() {
		JsonNode jsonNode = request().body().asJson();
		String formDataJson =  jsonNode.findPath("data").toString();
		System.out.println("In processMediaData()- " + formDataJson);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			UniappMediaData uniappMediaData = objectMapper.readValue(formDataJson, UniappMediaData.class);
			boolean result = dataProcessingService.processImageData(uniappMediaData);
			return ok(Json.toJson(result));  
		}  catch (IOException e) {
			Logger.error("In processTextData()- IO Exception");
			e.printStackTrace();
		}
		return ok(Json.toJson(false));
	}
	
	public Result processMediaData1() {
	
		Http.MultipartFormData body = request().body().asMultipartFormData();
		Map<String, String[]> headersMapJson = request().headers();
		Map<String, String[]> requestMap = body.asFormUrlEncoded();
		Map<String, String> headersMap = generateHeadersMap(headersMapJson);
		if(requestMap == null) {
			Logger.error("In processImageData() - RequestMap is NULL");
			return ok(Json.toJson(false));
		} else if(!checkFields(headersMap)) {
			Logger.error("In processImageData() - Not all required fields found");
			return ok(Json.toJson(false));
		} else {
			List<FilePart> files = body.getFiles();
			for (Iterator<FilePart> iterator = files.iterator(); iterator.hasNext();) {
				FilePart filePart = (FilePart) iterator.next();
				File file = filePart.getFile();
				System.out.println("In processImageData() - Size of media recieved - " + file.length() + "\n Absolute Path of the file - " + file.getAbsolutePath());
				UniappMediaData uniappMediaData = new UniappMediaData();
				byte[] imageContent = new byte[(int) file.length()];
				try {
					imageContent = Files.readAllBytes(file.toPath());
					uniappMediaData.setMediaContent(imageContent);
					uniappMediaData.setSuperAppId(UUIDUtils.toUUID(headersMap.get(FieldConstants.SUPER_APP_ID)));
					uniappMediaData.setAppId(UUIDUtils.toUUID(headersMap.get(FieldConstants.APP_ID)));
					uniappMediaData.setProjectId(UUIDUtils.toUUID(headersMap.get(FieldConstants.PROJECT_INTERNAL_ID)));
					uniappMediaData.setFieldUUID(UUIDUtils.toUUID(headersMap.get(FieldConstants.FIELD_ID)));
					uniappMediaData.setLatitude(Double.parseDouble(headersMap.get(FieldConstants.LATITUDE)));
					uniappMediaData.setLongitude(Double.parseDouble(headersMap.get(FieldConstants.LONGITUDE)));
					uniappMediaData.setSyncTimeStamp(Long.parseLong(headersMap.get(FieldConstants.SYNC_TIMESTAMP)));
					uniappMediaData.setTokenId(UUIDUtils.toUUID(headersMap.get(FieldConstants.TOKEN)));
					uniappMediaData.setUserId(headersMap.get(FieldConstants.USER_ID));
					uniappMediaData.setProjectExternalId(headersMap.get(FieldConstants.EXTERNAL_PROJECT_ID));
					uniappMediaData.setMediaPath(headersMap.get(FieldConstants.MEDIA_PATH));
					boolean result = dataProcessingService.processImageData(uniappMediaData);
					return ok(Json.toJson(result));  
				} catch (IOException e) {
					Logger.error("In processImageData() - IO Exception");
					e.printStackTrace();
				}
			}
		}
		return ok(Json.toJson(false));
	}

	private Map<String, String> generateHeadersMap(Map<String, String[]> headersMapJson) {
		Map<String, String> headersMap = new HashMap<>();
		for(String key : headersMapJson.keySet()) {
			if(key.equals("data")) {
				String[] dataNode = headersMapJson.get("data");
				try {
					headersMap = new ObjectMapper().readValue(dataNode[0], new TypeReference<Map<String, String>>(){});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return headersMap;
	}

	private boolean checkFields(Map<String, String> headersMap) {
		if(headersMap.containsKey(FieldConstants.SUPER_APP_ID)
				&& headersMap.containsKey(FieldConstants.APP_ID)
				&& headersMap.containsKey(FieldConstants.PROJECT_INTERNAL_ID)
				&& headersMap.containsKey(FieldConstants.FIELD_ID)
				&& headersMap.containsKey(FieldConstants.LATITUDE)
				&& headersMap.containsKey(FieldConstants.LONGITUDE)
				&& headersMap.containsKey(FieldConstants.SYNC_TIMESTAMP)
				&& headersMap.containsKey(FieldConstants.TOKEN)
				&& headersMap.containsKey(FieldConstants.USER_ID)
				&& headersMap.containsKey(FieldConstants.EXTERNAL_PROJECT_ID)
				&& headersMap.containsKey(FieldConstants.MEDIA_PATH)) {
			return true;
		}
		System.out.println("HeadersMap-" + headersMap);
		return false;
	}

}
