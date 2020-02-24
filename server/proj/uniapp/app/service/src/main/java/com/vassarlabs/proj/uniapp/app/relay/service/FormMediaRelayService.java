package com.vassarlabs.proj.uniapp.app.relay.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.rest.call.object.RequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageGeotagData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageSubmittedAndReceivedData;
import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ImageGeotagDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ImageSubmittedAndReceivedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.MediaRelayStates;
import com.vassarlabs.proj.uniapp.utils.api.pojo.APIList;
import com.vassarlabs.proj.uniapp.validations.api.pojo.AsyncExternalApiCallService;

@Component
public class FormMediaRelayService {

	@Autowired ApplicationMetaDataCrudService applicationMetaDataCrudService;

	@Autowired AsyncExternalApiCallService externalApiCallService;
	@Autowired ImageSubmittedAndReceivedDataCrudService mediaDataStatusTrackCrudService;
	
	@Autowired ApplicationProperties properties;

	@Autowired ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	@Autowired ImageGeotagDataCrudService mediaCrudService;

	@Autowired private IVLLogService logFactory;
	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	ObjectMapper objectMapper = new ObjectMapper();

	public List<ServiceOutputObject> relayMediaToExternalServer(FormMediaValue formMediaValue) throws  IOException, InterruptedException {

		List<ServiceOutputObject> outputList  = new ArrayList<ServiceOutputObject>();
		// Do not read from DB, it should be passed to the method - FormMediaValue
		//ImageGeotagData dbMediaObject= mediaCrudService.findImageGeotagDataByPrimaryKey(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getProjectId(), formMediaValue.getMediaUUID());
		ImageSubmittedAndReceivedData mediaDataRelayStatus = mediaDataStatusTrackCrudService.findImageSubmittedAndReceivedDataByPrimaryKey(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getProjectId(), formMediaValue.getMediaUUID());
		String submittedObjectJsonFormat = objectMapper.writeValueAsString(formMediaValue);
		//This should be cached - read from db only once - to do later for now get this from a separate service
		APIList apilistObject = applicationMetaDataCrudService.getExternalAPIList(formMediaValue.getSuperAppId(), formMediaValue.getAppId());
		if(apilistObject.getApiList() == null) {
			logger.debug("No API to relay data to External Server");
			ServiceOutputObject outputObject = new ServiceOutputObject();
			UserTrackingObject trackingObject = new UserTrackingObject(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getUserId(), formMediaValue.getTokenId(),
					APITypes.FORM_MEDIA_RELAY, "No API to relay data to External Server", "", null, true, formMediaValue.getSyncTimeStamp());
			outputObject.setTrackingObject(trackingObject);
			outputObject.setSuccessful(true);
			outputList.add(outputObject);
			return outputList;
		}
		List<RequestObject> listOfAPIs = apilistObject.getApiList().get(APITypes.MEDIA_UPLOAD.getValue());
		if (listOfAPIs == null || listOfAPIs.isEmpty()) {
			ServiceOutputObject outputObject = new ServiceOutputObject();
			UserTrackingObject trackingObject = new UserTrackingObject(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getUserId(), formMediaValue.getTokenId(),
					APITypes.FORM_MEDIA_RELAY, "No API to relay data to External Server", "", null, true, formMediaValue.getSyncTimeStamp());
			outputObject.setTrackingObject(trackingObject);
			outputObject.setSuccessful(true);
			logger.debug("No API to relay data to External Server");
			outputList.add(outputObject);
			return outputList;
		}
		if(mediaDataRelayStatus != null
				&& mediaDataRelayStatus.getRelayStatus() != null
				&& mediaDataRelayStatus.getRelayStatus() == MediaRelayStates.FORM_DATA_RELAYED.getValue()) {
			/*MultiValueMap<String, Object> multiPartBody = new LinkedMultiValueMap<String, Object>();
			String imageFilePath = properties.getProperty("images-file-path");
			File file = new File(imageFilePath + "/" + formMediaValue.getFieldUUID().toString());
			ByteBuffer buf  = dbMediaObject.getImageData();
			byte[] arr = new byte[buf.remaining()];
			buf.get(arr);
			Files.write(arr, file);
			FileSystemResource resource = new FileSystemResource(file);
			multiPartBody.add(formMediaValue.getFieldUUID().toString(), resource);*/
			for(RequestObject requestObject : listOfAPIs) {
				/*Map<String, Object> headersMap = createHeadersForImageGeotagData(formMediaValue, dbMediaObject);
				requestObject.setMultiValueParams(multiPartBody);
				requestObject.setHeaders(headersMap);*/
				Map<String, Object> params = requestObject.getParams();
				if(params == null) {
					params = new HashMap<>();
				}
				params.put("data", formMediaValue);
				requestObject.setParams(params);
				int retryCount = 0;
				while(true) {
					++retryCount;
					try {
						logger.debug("Calculating Attempt-" + retryCount);
						CompletableFuture<String> response = externalApiCallService.callAPImethod(requestObject);
						if(response == null || response.get() == null) {
							if (retryCount >= CommonConstants.MAX_RETRIES) {
								logger.error("Max retries reached... Aborting");
								addUnsucessfulTrackingObjectToList(outputList,formMediaValue, requestObject, formMediaValue.getSyncTimeStamp());
								break;
							}
						} else {
							Boolean result = Boolean.valueOf(response.get());
							if(result) {
								logger.debug("Data relayed successfully : Request param:" + requestObject);
								ServiceOutputObject outputObject = new ServiceOutputObject();
								UserTrackingObject trackingObject = new UserTrackingObject(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getUserId(), formMediaValue.getTokenId(),
										APITypes.FORM_MEDIA_RELAY, "Data inserted to uniapp server and successfully relayed to URL-" + requestObject.getUrl(), objectMapper.writeValueAsString(requestObject), null, true, formMediaValue.getSyncTimeStamp());
								outputObject.setTrackingObject(trackingObject);
								outputObject.setSuccessful(true);
								outputList.add(outputObject);
								
								updateMediaRelayStatus(formMediaValue);
								break;
							} else {
								if (retryCount >= CommonConstants.MAX_RETRIES) {
									logger.error("Max retries reached... Aborting");
									addUnsucessfulTrackingObjectToList(outputList,formMediaValue, requestObject, formMediaValue.getSyncTimeStamp());
									break;
								}
							}
						}
					} catch(Exception e) {
						logger.error("Exception occured while relaying media data...");
						e.printStackTrace();
						if (retryCount >= CommonConstants.MAX_RETRIES) {
							logger.error("Max retries reached... Aborting");
							addUnsucessfulTrackingObjectToList(outputList,formMediaValue, requestObject, formMediaValue.getSyncTimeStamp());
							e.printStackTrace();
							break;
						} 
					}
					//Sleep here before next retry
					logger.debug("Sleeping for " + CommonConstants.THREAD_SLEEP_TIME + " before retrying again...");
					Thread.sleep(CommonConstants.THREAD_SLEEP_TIME);
				}
				//file.delete();
			} 
		} else if(mediaDataRelayStatus != null
				&& mediaDataRelayStatus.getRelayStatus() != null
				&& mediaDataRelayStatus.getRelayStatus() == MediaRelayStates.FILE_DATA_RALYED.getValue()) { 
			ServiceOutputObject outputObject = new ServiceOutputObject();
			UserTrackingObject trackingObject = new UserTrackingObject(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getUserId(), formMediaValue.getTokenId(),
					APITypes.FORM_MEDIA_RELAY, "Media " + formMediaValue.getMediaUUID() + " is already relayed " , submittedObjectJsonFormat, null, true, formMediaValue.getSyncTimeStamp());
			outputObject.setTrackingObject(trackingObject);
			outputObject.setSuccessful(true);
			logger.debug("Media is already relayed");
			outputList.add(outputObject);
		}
		else {
			ServiceOutputObject outputObject = new ServiceOutputObject();
			UserTrackingObject trackingObject = new UserTrackingObject(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getUserId(), formMediaValue.getTokenId(),
					APITypes.FORM_MEDIA_RELAY, "Text Data for Media " + formMediaValue.getMediaUUID() + " is not relayed yet." , submittedObjectJsonFormat, null, false, formMediaValue.getSyncTimeStamp());
			outputObject.setTrackingObject(trackingObject);
			outputObject.setSuccessful(false);
			logger.debug("Text Data for Media is not relayed yet.");
			outputList.add(outputObject);
		}
		return outputList;

	}
	
	/*private Map<String, Object> createHeadersForImageGeotagData(FormMediaValue formData, ImageGeotagData dbMediaObject) {
		Map<String, Object> headersMap = new HashMap<>();
		headersMap.put("super_app", formData.getSuperAppId());
		headersMap.put("app_id", formData.getAppId());
		headersMap.put("user_id", formData.getUserId());
		headersMap.put("token", formData.getTokenId());
		headersMap.put("fld_id", formData.getFieldUUID());
		headersMap.put("proj_id", formData.getProjectId());
		headersMap.put("lat", formData.getLatitude());
		headersMap.put("long", formData.getLongitude());
		headersMap.put("media_type", formData.getMediaType());
		headersMap.put("sync_ts", formData.getSyncTimeStamp());
		headersMap.put("gps_accuracy", formData.getGpsAccuracy());
		headersMap.put("timestamp_overlay", formData.getTimestampOverlay());
		headersMap.put("media_path", dbMediaObject.getMediaPath());
		ProjectExternalInternalMapData projectExternalInternalMapData = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataForProjectId(formData.getSuperAppId(), formData.getAppId(), formData.getProjectId());
		headersMap.put("ext_proj_id", projectExternalInternalMapData.getProjectExternalId());
		return headersMap;
	}*/
	private void addUnsucessfulTrackingObjectToList(List<ServiceOutputObject> outputList, FormMediaValue formMediaValue, RequestObject requestObject, long timeStamp) 
			throws JsonProcessingException {
		
		List<String> errorList = new ArrayList<>();
		errorList.add("Failed to relay data to external servers");
		UserTrackingObject trackingObject = new UserTrackingObject(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getUserId(), formMediaValue.getTokenId(),
				APITypes.FORM_MEDIA_RELAY, "Data not relayed to URL-" + requestObject.getUrl(), objectMapper.writeValueAsString(requestObject), null, false, timeStamp);
		ServiceOutputObject outputObject = new ServiceOutputObject();
		outputObject.setTrackingObject(trackingObject);
		outputObject.setSuccessful(false);
		outputList.add(outputObject);
		
	}
	
	private void updateMediaRelayStatus(FormMediaValue formMediaValue) {
		mediaDataStatusTrackCrudService.updateRelayStatus(MediaRelayStates.FILE_DATA_RALYED.getValue(), formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getProjectId(), formMediaValue.getMediaUUID());
	}
}
