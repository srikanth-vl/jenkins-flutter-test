package com.vassarlabs.proj.uniapp.utility;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.CreateMapBBoxRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.LocationCoordinateObject;
import com.vassarlabs.proj.uniapp.api.pojo.MapConfig;
import com.vassarlabs.proj.uniapp.app.project.listload.BoundingBoxFindingService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.MapConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.MapConfigDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class BoundingBoxFormation {
	
	@Autowired UserMetaDataCrudService userDataCrudService;
	@Autowired ApplicationMetaDataCrudService appMetaDataCrudService;
	@Autowired UserProjectMapCrudService userProjectCrudService;
	@Autowired ProjectMasterDataCrudService projectMasterCrudService;
	@Autowired BoundingBoxFindingService bboxFinder;
	@Autowired MapConfigDataCrudService mapConfigDataCrudService;
	public boolean bboxFormationForSuperApp(CreateMapBBoxRequestObject requestObject) throws JsonProcessingException {
			
		Map<String, Set<String>> userIdtoMapFiles = new HashMap<>();
		if (requestObject.getSuperAppId() == null) {
			return false;
		}
		UUID superAppId = requestObject.getSuperAppId();
		UUID appId = requestObject.getAppId();
		List<LocationCoordinateObject> coordinatesForApp = new ArrayList<LocationCoordinateObject>();
		List<UserProjectMapping> userProjectList  = new ArrayList<>();
		String mapBaseUrl =  getMapBaseUrl(superAppId);
		if(mapBaseUrl ==  null || mapBaseUrl.isEmpty()) {
			return false;
		}
		if(requestObject.getUserIds() == null || requestObject.getUserIds().isEmpty()) {
			userProjectList = userProjectCrudService.findUserProjectMappingByPartitionKey(superAppId, appId);
		} else {
			userProjectList = userProjectCrudService.findUserProjectMappingByGivenUsers(superAppId, appId, requestObject.getUserIds());
		}
			
			Map<String, List<UUID>> userProjectIdsMap = new HashMap<String, List<UUID>>();
			
			for (UserProjectMapping userMapping : userProjectList) {
				List<UUID> projectIds = new ArrayList<UUID>();
				if (userProjectIdsMap.containsKey(userMapping.getUserId())) {
					projectIds = userProjectIdsMap.get(userMapping.getUserId());					
				}
				projectIds.addAll(userMapping.getProjectList());
				userProjectIdsMap.put(userMapping.getUserId(), projectIds);
			}
			
			for (String userId : userProjectIdsMap.keySet()) {
				List<LocationCoordinateObject> coordinates = new ArrayList<LocationCoordinateObject>();
				Map<UUID, Map<String, ProjectMasterData>> projectDataMap = projectMasterCrudService.getAllMasterDataForProjectIds(superAppId, appId, userProjectIdsMap.get(userId));
				for (UUID projectId : projectDataMap.keySet()) {
					if (projectDataMap.get(projectId).get(MasterDataKeyNames.GEO_TAG_KEY) != null) {
						LocationCoordinateObject locObject = new LocationCoordinateObject();
						ProjectMasterData masterData = projectDataMap.get(projectId).get(MasterDataKeyNames.GEO_TAG_KEY);
						List<String> coords = StringUtils.getStringListFromDelimitter(",", masterData.getValue());
						Double lat = Double.valueOf(coords.get(0));
						Double lon = Double.valueOf(coords.get(1));
						if (!(lat == 0.0 || lon == 0.0)) {
							locObject.setLat(Double.valueOf(getValueInGivenFormat(coords.get(0))));
							locObject.setLon(Double.valueOf(getValueInGivenFormat(coords.get(1))));
							coordinates.add(locObject);
							coordinatesForApp.add(locObject);
						}
					}					
				}
				Collections.sort(coordinates);
//				TODO: Sort the coordinates If needed
				bboxCreation(superAppId, UUIDUtils.getDefaultUUID(), userId, coordinates, userIdtoMapFiles, mapBaseUrl);
				
			}
			updateUserMapFiles(userIdtoMapFiles, superAppId);
		
		return true;
	}
	
	public void bboxCreation (UUID superAppId, UUID appId, String userId, List<LocationCoordinateObject> coordinates,Map<String, Set<String>> userIdtoMapFiles, String mapBaseUrl ) throws JsonProcessingException {
		
		if (coordinates.size() == 0) {
			return;
		}
		List<LocationCoordinateObject> newCoordinates = new ArrayList<LocationCoordinateObject>();
		List<LocationCoordinateObject> oldCoordinates = new ArrayList<LocationCoordinateObject>();
		LocationCoordinateObject basePoint = coordinates.get(0);
		coordinates.remove(0);
		Double north = basePoint.getLat() + MapConstants.bboxRadius;
		Double south = basePoint.getLat() - MapConstants.bboxRadius;
		Double east  = basePoint.getLon() + MapConstants.bboxRadius;
		Double west  = basePoint.getLon() - MapConstants.bboxRadius;
		
		for (LocationCoordinateObject coordinate : coordinates) {
			if ((south <= coordinate.getLat()) && (coordinate.getLat() <= north) && 
					(west <= coordinate.getLon()) && (coordinate.getLon() <= east)) {
				newCoordinates.add(coordinate);
			} else {
				oldCoordinates.add(coordinate);
			}
		}
		if (newCoordinates.size() != 0) {
			bboxFinder.bBoxForListOfCoordinates(superAppId, appId, userId, newCoordinates,userIdtoMapFiles, mapBaseUrl);
			bboxCreation(superAppId, appId, userId, oldCoordinates, userIdtoMapFiles, mapBaseUrl);
		}
		
	}
	public String getValueInGivenFormat(String value) {
		String formatedValue = "" ;
		if (value == null) {
			return formatedValue;
		} else  {
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.CEILING);
			Double dbvalue = Double.parseDouble(value);
			formatedValue = df.format(dbvalue);
		} 
		return formatedValue;	
	}
	public String getMapBaseUrl(UUID superAppId) {
		String mapConfigData = null;
		try {
			mapConfigData = mapConfigDataCrudService.getMapConfiguration(superAppId, UUIDUtils.getDefaultUUID());
		} catch (DataAccessException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MapConfig config = null;
		ObjectMapper mapper  = new ObjectMapper();
		if(mapConfigData != null && !mapConfigData.isEmpty()) {
			try {
				config = mapper.readValue(mapConfigData, MapConfig.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 

		String mapBaseUrl = null;
		if(config!= null) {
			mapBaseUrl = config.getMapBaseUrl();
		}
		return mapBaseUrl;
	}
	public void updateUserMapFiles(Map<String, Set<String>> userIdtoMapFiles, UUID superAppId) {
//		List<String> userIds = new ArrayList<>(userIdtoMapFiles.keySet());
//		Map<String,UserDBMetaData> users = userDataCrudService.getMetaDataForListOfUsers(superAppId, userIds, UserStates.ACTIVE);
//		for (String userId : userIdtoMapFiles.keySet()) {
//			UserDBMetaData userdbData = users.get(userId);
//			if(userIdtoMapFiles.get(userId) != null && !userIdtoMapFiles.get(userId).isEmpty()) {
//				List<String> fileUrls =  new ArrayList<>(userIdtoMapFiles.get(userId));
//				Map<UUID,String> appToFilesMap =  new HashMap<>();
//				String mapFilesString = StringUtils.getconcatenatedStringFromStringList(",", fileUrls);
//				appToFilesMap.put(UUIDUtils.getDefaultUUID(), mapFilesString);
//				userDataCrudService.updateMapFiles(superAppId, userId, userdbData.getUserExtId(), appToFilesMap);
//			}
//		}
	}
}
