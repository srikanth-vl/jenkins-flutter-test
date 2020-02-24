package com.vassarlabs.proj.uniapp.app.relay.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.prod.rest.call.object.RequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataToRelay;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserData;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.kafka.recordprocessor.service.FormMediaProcessingService;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ImageSubmittedAndReceivedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.utils.api.pojo.APIList;
import com.vassarlabs.proj.uniapp.validations.api.pojo.AsyncExternalApiCallService;

@Component
public class UserDataRelayService {
	
	@Autowired 
	SuperAppDataCrudService superAppDataCrudService;
	
	@Autowired private AsyncExternalApiCallService externalApiCallService;
	@Autowired ImageSubmittedAndReceivedDataCrudService mediaDataStatusTrackCrudService;
	@Autowired FormMediaProcessingService imageSubmittedAndReceivedDataProcessingService;
	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	ObjectMapper mapper = new ObjectMapper();
	
	public List<ServiceOutputObject> relayDataToExternalServer(UserData userDataObject) throws  JsonParseException, JsonMappingException, 
		IOException, InterruptedException {
		
		List<ServiceOutputObject> outputList  = new ArrayList<ServiceOutputObject>();
		UUID superAppId = userDataObject.getSuperAppId();	
		APIList apilistObject = superAppDataCrudService.getApiListConfig(superAppId);
		int relayRetries = 3;
		if(relayRetries >= CommonConstants.MAX_RELAY_RETRIES) {
			logger.debug("Maximum Retries for relay of data exceeds, Discarding data");
			ServiceOutputObject outputObject = new ServiceOutputObject();
			UserTrackingObject trackingObject = new UserTrackingObject(userDataObject.getSuperAppId(), UUIDUtils.getDefaultUUID(), userDataObject.getUserId(), UUIDUtils.getDefaultUUID(),
					APITypes.USER_PASSWORD_CHANGE, "Maximum Retries for relay of data exceeds, Discarding data", mapper.writeValueAsString(userDataObject), null, true, System.currentTimeMillis());
			outputObject.setTrackingObject(trackingObject);
			outputObject.setSuccessful(true);
			outputList.add(outputObject);
			return outputList;
		}
		if(apilistObject.getApiList() == null) {
			logger.debug("No API to relay data to External Server");
			ServiceOutputObject outputObject = new ServiceOutputObject();
			UserTrackingObject trackingObject = new UserTrackingObject(userDataObject.getSuperAppId(), UUIDUtils.getDefaultUUID(), userDataObject.getUserId(), UUIDUtils.getDefaultUUID(),
					APITypes.USER_PASSWORD_CHANGE, "No API to relay data to External Server", mapper.writeValueAsString(userDataObject), null, true, System.currentTimeMillis());
			outputObject.setTrackingObject(trackingObject);
			outputObject.setSuccessful(true);
			outputList.add(outputObject);
			return outputList;
		}
		List<RequestObject> listOfAPIs = apilistObject.getApiList().get(APITypes.USER_PASSWORD_CHANGE.getValue());
		if (listOfAPIs == null || listOfAPIs.isEmpty()) {
			logger.debug("No API to relay data to External Server");
			ServiceOutputObject outputObject = new ServiceOutputObject();
			UserTrackingObject trackingObject = new UserTrackingObject(userDataObject.getSuperAppId(), UUIDUtils.getDefaultUUID(), userDataObject.getUserId(), UUIDUtils.getDefaultUUID(),
					APITypes.USER_PASSWORD_CHANGE, "No API to relay data to External Server", "", null, true, System.currentTimeMillis());
			outputObject.setTrackingObject(trackingObject);
			outputObject.setSuccessful(true);
			outputList.add(outputObject);
			return outputList;
		}
		
		
		for(RequestObject requestObject : listOfAPIs) {
			Map<String, Object> params = requestObject.getParams();
			if(params == null) {
				params = new HashMap<>();
			}
			params.put("data", userDataObject);
			requestObject.setParams(params);
			int retryCount = 0;
			logger.info("Calculating Attempt-" + retryCount);
			while(true) {
				retryCount++;
				try {
					CompletableFuture<String> response = externalApiCallService.callAPImethod(requestObject);
					if(response == null || response.get() == null) {
						if (retryCount >= CommonConstants.MAX_RETRIES) {
							addUnsucessfulTrackingObjectToList(outputList, userDataObject, requestObject, System.currentTimeMillis());
							logger.error("Max retries reached... Aborting");
							break;
						}
					} else {
						Boolean result = Boolean.valueOf(response.get());
						if(result) {
							logger.debug("Data relayed successfully : Request param:" + requestObject);
							logger.debug("Result from third party - " + result);
							UserTrackingObject trackingObject = new UserTrackingObject(userDataObject.getSuperAppId(), UUIDUtils.getDefaultUUID(), userDataObject.getUserId(), UUIDUtils.getDefaultUUID(),
									APITypes.USER_PASSWORD_CHANGE, "Data successfully relayed to URL-" + requestObject.getUrl(), mapper.writeValueAsString(requestObject), null, true, System.currentTimeMillis());
							ServiceOutputObject outputObject = new ServiceOutputObject();
							outputObject.setTrackingObject(trackingObject);
							outputObject.setSuccessful(true);
							outputList.add(outputObject);
							
							break;
						} else {
							if (retryCount >= CommonConstants.MAX_RETRIES) {
								logger.error("Max retries reached... Aborting");
								addUnsucessfulTrackingObjectToList(outputList, userDataObject, requestObject, System.currentTimeMillis());
								break;
							}
						}
					}
				} catch(Exception e) {
					logger.error("Exception occured while relaying");
					e.printStackTrace();
					if (retryCount >= CommonConstants.MAX_RETRIES) {
						addUnsucessfulTrackingObjectToList(outputList, userDataObject, requestObject, System.currentTimeMillis());
						logger.error("Max retries reached... Aborting");
						break;
					}
				}
				logger.debug("Sleeping for " + CommonConstants.THREAD_SLEEP_TIME + " before retrying again...");
				Thread.sleep(CommonConstants.THREAD_SLEEP_TIME);
			}
		}
		
		return outputList;
	}

	private void addUnsucessfulTrackingObjectToList(List<ServiceOutputObject> outputList, UserData userDataObject, RequestObject requestObject, long timeStamp) 
			throws JsonProcessingException {
		
		List<String> errorList = new ArrayList<>();
		errorList.add("Failed to relay data to external servers");
		UserTrackingObject trackingObject = new UserTrackingObject(userDataObject.getSuperAppId(), UUIDUtils.getDefaultUUID(), userDataObject.getUserId(), UUIDUtils.getDefaultUUID(),
				APITypes.USER_PASSWORD_CHANGE, "Data not relayed to URL-" + requestObject.getUrl(), mapper.writeValueAsString(requestObject), errorList, false, timeStamp);
		ServiceOutputObject outputObject = new ServiceOutputObject();
		outputObject.setTrackingObject(trackingObject);
		outputObject.setSuccessful(false);
		outputList.add(outputObject);
		
	}

}
