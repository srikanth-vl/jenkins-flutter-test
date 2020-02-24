package controllers.com.vassarlabs.play.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.MediaDownloadRequestParams;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.api.IMediaDownloadService;
import com.vassarlabs.proj.uniapp.app.insert.service.IMediaReceiverService;
import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;

import controllers.com.vassarlabs.play.constants.ResponseConstants;
import models.com.vassarlabs.play.models.ApiResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import services.com.vassarlabs.play.services.MediaDataGenerationUtils;

@org.springframework.stereotype.Controller
public class MediaReceiverServiceController 
extends BasicController {

	@Autowired ApplicationContext applicationCtxt;	

	@Autowired IMediaDownloadService mediaDownloadService;

	@Autowired MediaDataGenerationUtils mediaDataGenerationUtils;
	
	@Autowired ApplicationProperties applicationProperties;
	
	public Result receiveAndSubmitData() {

		ApiResponse response = new ApiResponse();
		Http.MultipartFormData body = request().body().asMultipartFormData();
		//get other properties read using a util/static method
		try {
			
			FormMediaValue mediaValue = mediaDataGenerationUtils.getMediaObject(body, response);
			if(mediaValue == null) {
				return ok(Json.toJson(response));
			}
			//Read once at start of the controller
			List<String> mediaRecieverClass = applicationProperties.getListProperty(CommonConstants.MEDIA_SUBMIT_RECEIVER_CLASS);
			List<IMediaReceiverService> mediaReceivers = new ArrayList<IMediaReceiverService>();
			
			/* 
			 * Initialize List of beans read from configuration files
			 */
			for (String classname : mediaRecieverClass) {
				mediaReceivers.add((IMediaReceiverService)applicationCtxt.getBean(classname));
			}
			/*
			 * First bean by specified in configuration file should be to Insert in DB
			 * Invoke IMediaReceiverService.execute() method to insert in DB.
			 */
			// Return on first error
			boolean isExecutionNeeded = true;
			for(int index = 0; index < mediaReceivers.size(); index++) {
				if(isExecutionNeeded) {
					ServiceOutputObject outputObject = mediaReceivers.get(index).execute(mediaValue);
					if(index == 0) {
						// Return response back to the app
						if(outputObject.isSuccessful()) {
							response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.IMAGE_SYNC_SUCCESS);
						} else {
							response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
						}
					}
					isExecutionNeeded = outputObject.isSuccessful();
				}
			}
		} catch (IOException e) {
			Logger.error("In submitMediaData - IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In submitMediaData - Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In submitMediaData - Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (ValidationException e) {
			Logger.error("In submitMediaData - Validation Exception ");
			response.setResult(false, ResponseConstants.VALIDATION_EXCEPTION_ERROR_CODE, null, ResponseConstants.VALIDATION_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (DataNotFoundException e) {
			Logger.error("In submitMediaData - Data Not found ");
			response.setResult(false, ResponseConstants.NO_DATA_FOUND_ERROR_CODE, null, ResponseConstants.NO_DATA_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (InterruptedException e) {
			Logger.error("In submitMediaData - Thread Interrupted Exception ");
			response.setResult(false, ResponseConstants.FAILURE, null, "");
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	public Result downloadMedia() {
		ApiResponse response = new ApiResponse();
		Http.MultipartFormData body = request().body().asMultipartFormData();

		Map<String, String[]> requestMap = body.asFormUrlEncoded();
		if(requestMap == null) {
			Logger.error("In downloadMedia() - RequestMap is NULL");
			response.setResult(false, ResponseConstants.REQUEST_MAP_NULL_ERROR_CODE, null, ResponseConstants.REQUEST_MAP_NULL_ERROR_MESSAGE);
		} else {
			UUID superAppId = getUUIDValue(requestMap, response, "superapp");
			UUID appId = getUUIDValue(requestMap, response, "appid");
			UUID projectId = getUUIDValue(requestMap, response, "projectid");
			UUID mediaId = getUUIDValue(requestMap, response, "mediaid");
			UUID token = getUUIDValue(requestMap, response, "token");
			String mediaType = getStringValue(requestMap, response, "mediatype");
			String userId = getStringValue(requestMap, response, "userid");

			if(superAppId == null || appId == null || projectId == null || mediaId == null 
					|| token == null ) {
				Logger.error("In submitImageGeotagData() - Found NULL parameters : superAppId : " + superAppId + " appId : " + appId + " projectId : "
						+ projectId + " media UUID : " + mediaId + " token : " + token);
				response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
				return ok(Json.toJson(response));
			}
			MediaDownloadRequestParams requestParams = new MediaDownloadRequestParams(projectId, mediaType, mediaId);
			requestParams.setSuperAppId(superAppId);
			requestParams.setAppId(appId);
			requestParams.setTokenId(token);
			requestParams.setUserId(userId);
			try {
				ServiceOutputObject outputObject = mediaDownloadService.downloadMedia(requestParams);
				return ok(Json.toJson(outputObject));
			} catch (IOException e) {
				Logger.error("In MediaDownloadService - IO Exception");
				response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		return ok(Json.toJson(response));
	}

}
