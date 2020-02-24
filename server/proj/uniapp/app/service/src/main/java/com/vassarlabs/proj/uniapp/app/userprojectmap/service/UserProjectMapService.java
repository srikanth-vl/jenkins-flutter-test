package com.vassarlabs.proj.uniapp.app.userprojectmap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.UserProjectMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapDBData;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;

@Component
public class UserProjectMapService {

	@Autowired
	UserProjectMapCrudService userProjectMapCrudService;
	
	public Map<String, List<UserProjectMapData>> getUserProjectMapDataList (UUID superAppId, UUID appId, Map<String, UUID> extToInternalIdMap) throws DataNotFoundException {
		
		Map<String, List<UserProjectMapData>> userIdToUserProjectDataMap;
		Map<UUID, List<UserProjectMapDBData>> userIdToUserProjectDBDataMap;
		List<UserProjectMapData> userProjectMapDataList;
		
		userIdToUserProjectDataMap = new HashMap<>();
		userIdToUserProjectDBDataMap = userProjectMapCrudService.getAllAssignedUsersForListOfProjects(superAppId, appId, new ArrayList<UUID>(extToInternalIdMap.values()));
		
		for (String extId : extToInternalIdMap.keySet()) {
			UUID internalId = extToInternalIdMap.get(extId);
			if(userIdToUserProjectDBDataMap.get(internalId) != null)
			{	
				userProjectMapDataList = new ArrayList<>();
				for(UserProjectMapDBData userProjectMapDBData : userIdToUserProjectDBDataMap.get(internalId)) {
					userProjectMapDataList.add(new UserProjectMapData(userProjectMapDBData.getUserId(), UserPriorities.getAPINameByValue(userProjectMapDBData.getUserType())));
				}
				userIdToUserProjectDataMap.put(extId, userProjectMapDataList);
			}
		}
		
		return userIdToUserProjectDataMap;
		
	}

}
