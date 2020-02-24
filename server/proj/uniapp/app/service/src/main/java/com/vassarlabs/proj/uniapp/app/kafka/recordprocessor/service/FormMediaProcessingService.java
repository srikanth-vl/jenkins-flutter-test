package com.vassarlabs.proj.uniapp.app.kafka.recordprocessor.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.MediaMetaData;
import com.vassarlabs.proj.uniapp.app.relay.service.FormMediaRelayService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageGeotagData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ImageGeotagDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ImageSubmittedAndReceivedDataCrudService;
import com.vassarlabs.proj.uniapp.enums.MediaRelayStates;

@Component
public class FormMediaProcessingService {

	@Autowired
	private IVLLogService logFactory;
	private IVLLogger logger;
	
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired private ImageSubmittedAndReceivedDataCrudService imageSubmittedAndReceivedDataCrudService;
	@Autowired private FieldMetaDataCrudService fieldMetaDataCrudService;
	@Autowired private ImageGeotagDataCrudService mediaDataCrudService;
	@Autowired private FormMediaRelayService formMediaRelayService;

	public void updateSubmittedImageData(AppFormDataSubmittedList appFormDataSubmittedList) {

		UUID superAppId = appFormDataSubmittedList.getSuperAppId();
		UUID applicationId = appFormDataSubmittedList.getAppId();
		List<AppFormData> appFormDataList = appFormDataSubmittedList.getAppFormDataList();
		Map<String, List<FieldMetaData>> latestMetaDataMap = fieldMetaDataCrudService.findMdInstanceIdTofieldMetaDataListMapForApp(superAppId, applicationId);
		Map<String, Map<String, String>> mdInstanceIdToKeyToValuesMap = new HashMap<>();

		for(AppFormData appFormData : appFormDataList) {
			if(appFormData == null) {
				logger.info("Form inserted data is null for superapp " + superAppId  + " and app " + applicationId);
			}
			UUID projectId = appFormData.getProjectId();
			List<FormFieldValues> formFieldValueList = appFormData.getFormFieldValuesList();
			long timestamp = appFormData.getTimeStamp();
			Map<String, String> keyToValuesMap;
			String mdInstanceId = appFormData.getMetaDataInstanceId();
			if(mdInstanceIdToKeyToValuesMap.get(mdInstanceId) == null){
				List<FieldMetaData> fieldMetaDataList = latestMetaDataMap.get(mdInstanceId);
				if(fieldMetaDataList == null || fieldMetaDataList.isEmpty()) {
					logger.info("Field meta data list is not present for form instance "+ mdInstanceId);
					continue;
				}
				keyToValuesMap = fieldMetaDataList.stream().collect(Collectors.toMap(FieldMetaData :: getKey, FieldMetaData :: getDataType, (oldValue, newValue) -> oldValue));
				mdInstanceIdToKeyToValuesMap.put(mdInstanceId, keyToValuesMap);
			}
			else {
				keyToValuesMap = mdInstanceIdToKeyToValuesMap.get(mdInstanceId);
			}

			for(FormFieldValues formFieldValue : formFieldValueList) {
				String dataKey = formFieldValue.getKey();
				if(keyToValuesMap == null || keyToValuesMap.get(dataKey) == null) {
					logger.info("No values corresponding to key " + mdInstanceId);
					continue;
				}
				if(keyToValuesMap.get(dataKey).equalsIgnoreCase(CommonConstants.DATATYPE_IMAGE) || keyToValuesMap.get(dataKey).equalsIgnoreCase(CommonConstants.DATATYPE_VIDEO)){
					List<String> imageListAsString = StringUtils.getStringListFromDelimitter(",", formFieldValue.getValue());
					for(String imageIdAsStringwithlonLat : imageListAsString) {
						String imageUuidAsString = StringUtils.getStringListFromDelimitter(CommonConstants.DELIMITER, imageIdAsStringwithlonLat).get(0);
						UUID imageId = UUIDUtils.toUUID(imageUuidAsString);
						imageSubmittedAndReceivedDataCrudService.updateSubmittedImageData(CommonConstants.STATUS_TRUE, superAppId, applicationId, projectId, imageId, timestamp, System.currentTimeMillis());
					}
				}
			}
		}
	}

	public void updateReceivedImageData(FormMediaValue imageGeoTagFormValue) {

		UUID superAppId = imageGeoTagFormValue.getSuperAppId();
		UUID applicationId = imageGeoTagFormValue.getAppId();
		UUID projectId = imageGeoTagFormValue.getProjectId();
		UUID imageId = imageGeoTagFormValue.getMediaUUID();
		long insTimestamp = imageGeoTagFormValue.getInsTimeStamp();
		imageSubmittedAndReceivedDataCrudService.updateReceivedImageData(CommonConstants.STATUS_TRUE, superAppId, applicationId, projectId, imageId, insTimestamp);
	}
	
	public void updateMediaRelayStatus(AppFormDataSubmittedList appFormDataSubmittedList) throws IOException, InterruptedException {
		UUID superAppId = appFormDataSubmittedList.getSuperAppId();
		UUID applicationId = appFormDataSubmittedList.getAppId();
		List<AppFormData> appFormDataList = appFormDataSubmittedList.getAppFormDataList();
		UUID tokenId= appFormDataSubmittedList.getTokenId();
		String userId = appFormDataSubmittedList.getUserId();
		Map<String, Map<String, String>> mdInstanceIdToKeyToValuesMap = fieldMetaDataCrudService.findMdInstanceIdTofieldMetaDataKeyToDataTypeMap(superAppId, applicationId);

		for(AppFormData appFormData : appFormDataList) {
			if(appFormData == null) {
				logger.info("Form inserted data is null for superapp " + superAppId  + " and app " + applicationId);
			}
			UUID projectId = appFormData.getProjectId();
			List<FormFieldValues> formFieldValueList = appFormData.getFormFieldValuesList();

			Map<String, String> keyToValuesMap;
			String mdInstanceId = appFormData.getMetaDataInstanceId();

			keyToValuesMap = mdInstanceIdToKeyToValuesMap.get(mdInstanceId);

			for(FormFieldValues formFieldValue : formFieldValueList) {
				String dataKey = formFieldValue.getKey();
				if(keyToValuesMap == null || keyToValuesMap.get(dataKey) == null) {
					logger.info("No values corresponding to key " + mdInstanceId);
					continue;
				}
				if(keyToValuesMap.containsKey(dataKey)
						&& keyToValuesMap.get(dataKey).equalsIgnoreCase(CommonConstants.DATATYPE_JSON_ARRAY)) {
//					logger.info("Processing FormJson :: " + dataKey  + ", DataType :: "  + keyToValuesMap.get(dataKey));
					String jsonArray = formFieldValue.getValue();
					JsonNode arrayNode = objectMapper.readTree(jsonArray);

					for(JsonNode node : arrayNode) {
						Iterator<String> iterator = node.fieldNames();
						while(iterator.hasNext()) {
							
							String eachKey = iterator.next();
							FormFieldValues jsonArrayKeyData = new FormFieldValues();
							jsonArrayKeyData.setKey(eachKey);
							jsonArrayKeyData.setDataType(keyToValuesMap.get(eachKey));
							jsonArrayKeyData.setValue(node.get(eachKey).asText());
							processSubmittedFormMedia(superAppId,applicationId, projectId,  keyToValuesMap, jsonArrayKeyData, tokenId, userId);
							
						}
					}					
				} else {
					processSubmittedFormMedia(superAppId,applicationId, projectId,  keyToValuesMap, formFieldValue,  tokenId, userId);
				}
//				if(keyToValuesMap.get(dataKey).equalsIgnoreCase(CommonConstants.DATATYPE_IMAGE) 
//						|| keyToValuesMap.get(dataKey).equalsIgnoreCase(CommonConstants.DATATYPE_VIDEO)){
//					List<String> imageListAsString = StringUtils.getStringListFromDelimitter(",", formFieldValue.getValue());
//					for(String imageIdAsString : imageListAsString) {
//						String uuid = StringUtils.getStringListFromDelimitter(CommonConstants.DELIMITER, imageIdAsString).get(0); 
//						UUID mediaUUID = UUIDUtils.toUUID(uuid);
//						imageSubmittedAndReceivedDataCrudService.updateRelayStatus(MediaRelayStates.FORM_DATA_RELAYED.getValue(), superAppId, applicationId, projectId, mediaUUID);
//						ImageGeotagData dbMediaObject = mediaDataCrudService.findImageGeotagDataByPrimaryKey(superAppId, applicationId, projectId, mediaUUID);
//						if(dbMediaObject != null) {
//							FormMediaValue mediaValue = convertDBDataToMediaValue(dbMediaObject);
//							formMediaRelayService.relayMediaToExternalServer(mediaValue);
//						}
//					}
//				}
			}
		}
	}

	public FormMediaValue convertDBDataToMediaValue(ImageGeotagData dbMediaObject, String userId, UUID tokenId) throws JsonParseException, JsonMappingException, IOException{
		
		FormMediaValue formMedia = new FormMediaValue();
        formMedia.setUserId(userId);
        formMedia.setTokenId(tokenId);
        formMedia.setSuperAppId(dbMediaObject.getSuperAppId());
        formMedia.setAppId(dbMediaObject.getAppId());
        formMedia.setMediaUUID(dbMediaObject.getFieldId());
        formMedia.setGpsAccuracy(dbMediaObject.getGpsAccuracy());
        //System.out.println("Media Type " + dbMediaObject.getMediaType());
        formMedia.setMediaType(dbMediaObject.getMediaType());
        formMedia.setLatitude(dbMediaObject.getLatitude());
        formMedia.setLongitude(dbMediaObject.getLongitude());
        formMedia.setSyncTimeStamp(dbMediaObject.getSyncTs());
        formMedia.setTimestampOverlay(dbMediaObject.getTimestampOverlay());
        formMedia.setProjectId(dbMediaObject.getProjectId());
        formMedia.setMediaPath(dbMediaObject.getMediaPath());
        MediaMetaData mediaMetaData = new MediaMetaData();
        String metaDataFromDB = dbMediaObject.getMediaMetaData();
        if(metaDataFromDB != null && !metaDataFromDB.isEmpty()) {
        	mediaMetaData = objectMapper.readValue(dbMediaObject.getMediaMetaData(), MediaMetaData.class);
        	formMedia.setMediaFileExtension(mediaMetaData.getExtension());
        	formMedia.setMediaSubtype(mediaMetaData.getSubType());
        }
		String additionalProperties =  dbMediaObject.getAdditionalProperties();
		if(additionalProperties != null && !additionalProperties.isEmpty()) {
			Map<String, String> otherParams = new ObjectMapper().readValue(additionalProperties, new TypeReference<Map<String, String>>(){});
			formMedia.setOtherParams(otherParams);
		}
        formMedia.setInsTimeStamp(dbMediaObject.getInsertTs());
        return formMedia;
    }
	public void processSubmittedFormMedia(UUID superAppId, UUID applicationId, UUID projectId, Map<String, String> keyToDataTypeMap,  FormFieldValues formFieldValue, UUID tokenId, String userId) throws IOException, InterruptedException{
		
		String dataKey = formFieldValue.getKey();
		if(keyToDataTypeMap.get(dataKey).equalsIgnoreCase(CommonConstants.DATATYPE_IMAGE) 
				|| keyToDataTypeMap.get(dataKey).equalsIgnoreCase(CommonConstants.DATATYPE_VIDEO)){
			if(formFieldValue.getValue() == null || formFieldValue.getValue().isEmpty()) {
				return;
			}
			List<String> imageListAsString = StringUtils.getStringListFromDelimitter(",", formFieldValue.getValue());
			for(String imageIdAsString : imageListAsString) {
				String uuid = StringUtils.getStringListFromDelimitter(CommonConstants.DELIMITER, imageIdAsString).get(0); 
				UUID mediaUUID = UUIDUtils.toUUID(uuid);
				imageSubmittedAndReceivedDataCrudService.updateRelayStatus(MediaRelayStates.FORM_DATA_RELAYED.getValue(), superAppId, applicationId, projectId, mediaUUID);
				ImageGeotagData dbMediaObject = mediaDataCrudService.findImageGeotagDataByPrimaryKey(superAppId, applicationId, projectId, mediaUUID);
				if(dbMediaObject != null) {
					FormMediaValue mediaValue = convertDBDataToMediaValue(dbMediaObject, userId, tokenId);
					formMediaRelayService.relayMediaToExternalServer(mediaValue);
				}
			}
		}
	}
}
