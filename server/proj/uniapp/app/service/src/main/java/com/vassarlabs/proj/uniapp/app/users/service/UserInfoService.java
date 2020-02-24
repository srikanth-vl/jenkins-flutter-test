package com.vassarlabs.proj.uniapp.app.users.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.UserDetails;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.UserConstants;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class UserInfoService {

	@Autowired UserMetaDataCrudService userMetaDataCrudService;

	ObjectMapper objectMapper = new ObjectMapper();
	
	public Map<String, Map<String, Object>> getUserInfoForListOfUsers (UUID superAppId, List<String> userIds) throws DataNotFoundException {
		
		Map<String, Map<String, Object>> userIdToUserDataMap;
		Map<String, UserDBMetaData> userIdToUserDBMetaDataMap;
		
		userIdToUserDataMap = new HashMap<>();
		
		if(userIds == null || userIds.isEmpty())
			return userIdToUserDataMap;
		
		userIdToUserDBMetaDataMap = userMetaDataCrudService.getMetaDataForListOfUsers(superAppId, userIds, UserStates.ACTIVE);
		
		for(String userId : userIds)
			userIdToUserDataMap.put(userId, getUserData(userIdToUserDBMetaDataMap.get(userId)));
		
		return userIdToUserDataMap;
		
	}

	private Map<String, Object> getUserData(UserDBMetaData userDBMetaData) {
		
		if(userDBMetaData == null)
			return null;
		
		Map<String, Object> userDetails = new HashMap<>();
		
		try {
			userDetails = objectMapper.convertValue(objectMapper.readValue(userDBMetaData.getUserDetails(), UserDetails.class), new TypeReference<HashMap<String, Object>>(){});
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}
		
		userDetails.put(UserConstants.USER_EXT_ID, userDBMetaData.getUserExtId());
		userDetails.put(UserConstants.MOB_NO, userDBMetaData.getMobileNumber());
		userDetails.put(UserConstants.DEPT_NAME, userDBMetaData.getDepartmentName());
		
		return userDetails;
	}
	
}
