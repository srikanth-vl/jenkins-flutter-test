package com.vassarlabs.proj.uniapp.imagelog.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.EntityDetails;
import com.vassarlabs.proj.uniapp.api.pojo.ImageSubmittedAndReceivedDashboardData;
import com.vassarlabs.proj.uniapp.api.pojo.MediaSubmissionLogRequestObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageSubmittedAndReceivedData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ImageSubmittedAndReceivedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.MediaRelayStates;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;

/**
 * 
 * @author aman
 *
 */
@Component
public class ImageSubmittedAndReceivedDataRetrievalService {

	@Autowired
	private IVLLogService logFactory;
	private IVLLogger logger;
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Autowired ImageSubmittedAndReceivedDataCrudService imageSubmittedAndReceivedDataCrudService;	
	@Autowired SuperAppDataCrudService superAppDataCrudService;
	@Autowired ApplicationMetaDataCrudService applicationMetaDataCrudService;
	@Autowired DataRetrievalService dataretrievalservice;	 
	
	ObjectMapper objectMapper = new ObjectMapper();

	public List<ImageSubmittedAndReceivedDashboardData> getImageSubmittedAndReceivedData(MediaSubmissionLogRequestObject appFormRequestObject) throws IOException{
		List<ImageSubmittedAndReceivedDashboardData> finalImageSubmittedAndReceivedDataList = new ArrayList<>();
		Map<UUID, ImageSubmittedAndReceivedDashboardData> projectIdToFinalDataMap = new HashMap<>();
		UUID superAppId = appFormRequestObject.getSuperAppId();
		UUID applicationId = appFormRequestObject.getAppid();
		long startTs = appFormRequestObject.getStartTs();
		long endTs = appFormRequestObject.getEndTs();
			
		/*
		 * Getting the image submitted and received data list
		 * Appending the image id to the corresponding list(submitted or received) and incrementing the corresponding count
		 * if the image already exists, otherwise, creating a new object for each superapp, app, project and adding it to the
		 * final data list
		 * 
		 */
		
		List<ImageSubmittedAndReceivedData> imageSubmittedAndReceivedDataListFromDB = imageSubmittedAndReceivedDataCrudService.findImageSubmittedAndReceivedDataByPartitionKey(superAppId, applicationId);
		List<UUID> projectListIds = new ArrayList<>();
		List<ImageSubmittedAndReceivedData> imageSubmittedAndReceivedDataList = new ArrayList<ImageSubmittedAndReceivedData>();
			for(ImageSubmittedAndReceivedData imageSubmittedData : imageSubmittedAndReceivedDataListFromDB) {
				if(imageSubmittedData.getSubmissionTs() == null) {
					//imageSubmittedAndReceivedDataList.add(imageSubmittedData);
					continue;
				} else if(imageSubmittedData.getSubmissionTs() > startTs && imageSubmittedData.getSubmissionTs() <= endTs) {
					imageSubmittedAndReceivedDataList.add(imageSubmittedData);
				}
			}
		
			for(ImageSubmittedAndReceivedData imageData:imageSubmittedAndReceivedDataList) {
				if(!projectListIds.contains(imageData.getProjectId())){
					projectListIds.add(imageData.getProjectId());
				}
			}

		Map<UUID,Map<String,String>> projectmapids = new HashMap<>();
		
		try {
			projectmapids = dataretrievalservice.getValueForAProject(superAppId,applicationId,projectListIds,ProjectStates.ALL);
			
		} catch (DataNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(ImageSubmittedAndReceivedData imageSubmittedAndReceivedData : imageSubmittedAndReceivedDataList) {
			UUID projectId = imageSubmittedAndReceivedData.getProjectId();

			ImageSubmittedAndReceivedDashboardData finalImageSubmittedAndReceivedDataObj;
			if( projectIdToFinalDataMap.get(projectId) == null) {
				finalImageSubmittedAndReceivedDataObj = new ImageSubmittedAndReceivedDashboardData();
				//Adding the newly created final data object to the final list if the project doesn't exist in the final data map
				finalImageSubmittedAndReceivedDataList.add(finalImageSubmittedAndReceivedDataObj);
				
				EntityDetails superAppEntityDetails = new EntityDetails();
				SuperApplicationData superAppData = superAppDataCrudService.findLatestVersion(superAppId);
				superAppEntityDetails.setEntityId(superAppId);
				if(superAppData != null) {
					superAppEntityDetails.setEntityName(superAppData.getName());
				}
				else {
					logger.info("Meta Data not present for superapp "+ superAppId);
				}
				finalImageSubmittedAndReceivedDataObj.setSuperAppDetails(superAppEntityDetails);

				EntityDetails applicationEntityDetails = new EntityDetails();
				ApplicationMetaData applicationMetaData = applicationMetaDataCrudService.findLatestAppDataByPartitionKey(superAppId,applicationId); 
					
				applicationEntityDetails.setEntityId(applicationId);
				if(applicationMetaData != null) {
					JsonNode jsonNode;
					try {
						jsonNode = objectMapper.readTree(applicationMetaData.getConfigData());
						String applicationName = jsonNode.get("name").asText();
						applicationEntityDetails.setEntityName(applicationName);
					} catch (IOException e) {
						logger.info("Error mapping application data for "+ applicationEntityDetails);
					}
				}
				else {
					logger.info("Meta Data not present for application "+ applicationMetaData);
				}
				finalImageSubmittedAndReceivedDataObj.setApplicationDetails(applicationEntityDetails);
			}
			else {
				finalImageSubmittedAndReceivedDataObj = projectIdToFinalDataMap.get(projectId);
			}

			EntityDetails projectEntityDetails = new EntityDetails();
			projectEntityDetails.setEntityId(projectId);
			Map<String,String> tempProjMap = new HashMap<>();
			tempProjMap = projectmapids.get(projectId);
			String projnname = tempProjMap != null ? tempProjMap.get(MasterDataKeyNames.PROJ_NAME_KEY) : null;
			projectEntityDetails.setEntityName(projnname);
			finalImageSubmittedAndReceivedDataObj.setProjectDetails(projectEntityDetails);
			
			projectIdToFinalDataMap.put(projectId, finalImageSubmittedAndReceivedDataObj);
			if(imageSubmittedAndReceivedData.getSubmitStatus() == CommonConstants.STATUS_TRUE) {
				List<UUID> submittedImages = finalImageSubmittedAndReceivedDataObj.getSubmittedImageIds() == null ? new ArrayList<>() :
					finalImageSubmittedAndReceivedDataObj.getSubmittedImageIds();
				submittedImages.add(imageSubmittedAndReceivedData.getImageId());
				finalImageSubmittedAndReceivedDataObj.setSubmittedCount(finalImageSubmittedAndReceivedDataObj.getSubmittedCount() + 1);
				finalImageSubmittedAndReceivedDataObj.setSubmittedImageIds(submittedImages);
			}
			if(imageSubmittedAndReceivedData.getReceiveStatus() == CommonConstants.STATUS_TRUE) {
				List<UUID> receivedImages = finalImageSubmittedAndReceivedDataObj.getReceivedImageIds() == null ? new ArrayList<>() :
					finalImageSubmittedAndReceivedDataObj.getReceivedImageIds();
				receivedImages.add(imageSubmittedAndReceivedData.getImageId());
				finalImageSubmittedAndReceivedDataObj.setReceivedCount(finalImageSubmittedAndReceivedDataObj.getReceivedCount() + 1);
				finalImageSubmittedAndReceivedDataObj.setReceivedImageIds(receivedImages);
			}
			if(imageSubmittedAndReceivedData.getRelayStatus() != null && (imageSubmittedAndReceivedData.getRelayStatus() == MediaRelayStates.FORM_DATA_RELAYED.getValue() || imageSubmittedAndReceivedData.getRelayStatus() == MediaRelayStates.FILE_DATA_RALYED.getValue())) {
				List<UUID> relayedImageData = finalImageSubmittedAndReceivedDataObj.getTextDataRelayed() == null ? new ArrayList<>() :
					finalImageSubmittedAndReceivedDataObj.getTextDataRelayed();
				relayedImageData.add(imageSubmittedAndReceivedData.getImageId());
				finalImageSubmittedAndReceivedDataObj.setTextDataRelayCount(finalImageSubmittedAndReceivedDataObj.getTextDataRelayCount() + 1);
				finalImageSubmittedAndReceivedDataObj.setTextDataRelayed(relayedImageData);
			}
			if(imageSubmittedAndReceivedData.getRelayStatus() != null && imageSubmittedAndReceivedData.getRelayStatus() == MediaRelayStates.FILE_DATA_RALYED.getValue()) {
				List<UUID> relayedImagefiles = finalImageSubmittedAndReceivedDataObj.getMediaFileRelayed() == null ? new ArrayList<>() :
					finalImageSubmittedAndReceivedDataObj.getMediaFileRelayed();
				relayedImagefiles.add(imageSubmittedAndReceivedData.getImageId());
				finalImageSubmittedAndReceivedDataObj.setMediaFileRelayCount(finalImageSubmittedAndReceivedDataObj.getMediaFileRelayCount() + 1);
				finalImageSubmittedAndReceivedDataObj.setMediaFileRelayed(relayedImagefiles);
			}
		}
		
		return finalImageSubmittedAndReceivedDataList;
		
	}
}
