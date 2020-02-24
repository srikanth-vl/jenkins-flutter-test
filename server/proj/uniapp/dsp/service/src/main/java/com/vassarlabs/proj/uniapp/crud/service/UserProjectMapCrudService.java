package com.vassarlabs.proj.uniapp.crud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapDBData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.dsp.repository.UserProjectMapRepository;

@Component
public class UserProjectMapCrudService {
	
	@Autowired UserProjectMapRepository repository;
	
	public void insertUserProjectMappingData(UserProjectMapping data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		List<UserProjectMapDBData> insertData = new ArrayList<UserProjectMapDBData>();
		List<UUID> projectIds = data.getProjectList();
		for (UUID projectId : projectIds ) {
			UserProjectMapDBData userProjectMap =  createUserProjectMapData(data.getSuperAppId(), data.getAppId(), projectId, data.getUserId(), data.getUserType(), data.getInsertTs());
			insertData.add(userProjectMap);
		}
    	repository.saveAll(insertData);
    }
	
    public void insertUserProjectMappingData(List<UserProjectMapping> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	
    	List<UserProjectMapDBData> insertData = new ArrayList<UserProjectMapDBData>();
    	for (UserProjectMapping userProjectListMapping: data) {
    		for (UUID projectId : userProjectListMapping.getProjectList()) {
    			UserProjectMapDBData userProjectMap =  createUserProjectMapData(userProjectListMapping.getSuperAppId(), userProjectListMapping.getAppId(), projectId, userProjectListMapping.getUserId(), userProjectListMapping.getUserType(), userProjectListMapping.getInsertTs());
    			insertData.add(userProjectMap);
    		}
    	}
    	repository.saveAll(insertData);
    }
    
    public List<UserProjectMapping> findUserProjectMappingByPartitionKey(UUID superAppId, UUID appId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	List<UserProjectMapDBData> userProjectMapsData = repository.findByPartitionKey(superAppId, appId);
    	if(userProjectMapsData == null)
    		return null;
    	else
    		return createUserProjectListMapping(userProjectMapsData);
    	//return repository.findByPartitionKey(superAppId, appId);
    }
    
    public UserProjectMapping findUserProjectMappingByPrimaryKey(UUID superAppId, UUID appId, String userId, int userType) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	List<UserProjectMapDBData> userProjectMapsData = repository.findByPrimaryKey(superAppId, appId, userId, userType);
    	if(userProjectMapsData == null)
    		return null;
    	else {
    		List<UserProjectMapping> data = createUserProjectListMapping(userProjectMapsData);
    		if(data.size() > 0 ) {
    			return data.get(0);
    		} else 
    			return null;
    	}
    	//return repository.findByPrimaryKey(superAppId, appId, userId, userType);
    }
    
    public List<UserProjectMapping> findUserProjectMappingByUserId(UUID superAppId, UUID appId, String userId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	List<UserProjectMapDBData> userProjectMapsData = repository.findByUserId(superAppId, appId, userId);
    	if(userProjectMapsData == null)
    		return null;
    	else
    		return createUserProjectListMapping(userProjectMapsData);
    	//return repository.findByUserId(superAppId, appId, userId);
    }
    
    public List<UserProjectMapDBData> findUserProjectMappingDBDataByUserId(UUID superAppId, UUID appId, String userId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	List<UserProjectMapDBData> userProjectMapsData = repository.findByUserId(superAppId, appId, userId);
    	if(userProjectMapsData == null)
    		return null;
    	else
    		return userProjectMapsData;
    }

	public void deleteAllRecords(UUID superAppId, UUID appId) {
		repository.deleteAllRecords(superAppId, appId);	
	}
	
	public void deleteAllRecordsForAUser(UUID superAppId, List<UUID> appIds, String userId) {
		for(UUID appID : appIds) {
			repository.deleteAllRecordsForAUser(superAppId, appID, userId);	
		}
	}
	
	public void deleteAllRecordsForUsersWithUserType(UUID superAppId, UUID appId, List<String> userIdList, int userType) {
		repository.deleteAllRecordsForUsersWithUserType(superAppId, appId, userIdList, userType);	
	}
	public UserProjectMapDBData createUserProjectMapData(UUID superAppId, UUID appId, UUID projectId, String userId, int userType, long insertTs) {
		UserProjectMapDBData userProjectMap =  new UserProjectMapDBData();
		userProjectMap.setSuperAppId(superAppId);
		userProjectMap.setAppId(appId);
		userProjectMap.setProjectId(projectId);
		userProjectMap.setUserId(userId);
		userProjectMap.setUserType(userType);
		userProjectMap.setInsertTs(insertTs);
		return userProjectMap;
	}
	public List<UserProjectMapping> createUserProjectListMapping(List<UserProjectMapDBData> userProjectMappings) {
		Map<String, List<UUID>> userToProjectsList = new HashMap<> ();
		for (UserProjectMapDBData userProjectMap :  userProjectMappings) {
			String  key = userProjectMap.getSuperAppId() + "##" + userProjectMap.getAppId() + "##" + userProjectMap.getUserId() + "##" + userProjectMap.getUserType() + "##" + userProjectMap.getInsertTs();
			if(userToProjectsList.containsKey(key)) {
				List<UUID> projectIds = userToProjectsList.get(key);
				projectIds.add(userProjectMap.getProjectId());
				userToProjectsList.put(key, projectIds);
			} else  {
				List<UUID> projectIds = new ArrayList<UUID>();
				projectIds.add(userProjectMap.getProjectId());
				userToProjectsList.put(key, projectIds);
			}
		}
		List<UserProjectMapping> data = new ArrayList<> ();
		for (String key : userToProjectsList.keySet()) {
			List<String> ids = StringUtils.getStringListFromDelimitter("##", key);
			UserProjectMapping upm = new UserProjectMapping();
			upm.setSuperAppId(UUIDUtils.toUUID(ids.get(0)));
			upm.setAppId(UUIDUtils.toUUID(ids.get(1)));
			upm.setUserId(ids.get(2));
			upm.setUserType(Integer.parseInt(ids.get(3)));
			upm.setInsertTs(Long.parseLong(ids.get(4)));
			upm.setProjectList(userToProjectsList.get(key));
			data.add(upm);
		}
		return data;
	}
	
	public List<UUID> getAllProjectIdsAssignedtoUsersForApp(UUID superAppId, UUID appId, List<String> userIds) {
		List<UUID> ids = new ArrayList<UUID>();
		List<UserProjectMapDBData> projects = repository.getAllAssignedProjectForApp(superAppId, appId, userIds);
		if(projects == null) {
			return ids;
		} 
		for (UserProjectMapDBData p : projects ) {
			if(!ids.contains(p.getProjectId())) {
				ids.add(p.getProjectId());
			}	
		}
		return ids;
	}
	
	public Map<UUID, List<UserProjectMapDBData>> getAllAssignedUsersForListOfProjects(UUID superAppId, UUID appId, List<UUID> projectIds) {
		
		if(projectIds == null || projectIds.isEmpty())
			new HashMap<>();
		
		Map<UUID, List<UserProjectMapDBData>> projectIdToUserProjectMapDBData;
		List<UserProjectMapDBData> userProjectsMapList;
		
		projectIdToUserProjectMapDBData = new HashMap<>();
		userProjectsMapList = repository.getAllAssignedUsersForListOfProjects(superAppId, appId, projectIds);
		projectIdToUserProjectMapDBData = userProjectsMapList.stream().collect(Collectors.groupingBy(UserProjectMapDBData::getProjectId));
		
		return projectIdToUserProjectMapDBData;
		
	}

	public void deleteAllRecordsForAProject(UUID superAppId, UUID appId, UUID projectId) {
		repository.deleteAllRecordsForAProject(superAppId, appId, projectId);	
	}
	public void deleteAllRecordsForGivenUsersForApp(UUID superAppId, UUID appId, List<String> userIds) {
		repository.deleteAllRecordsForGivenUsersForApp(superAppId, appId, userIds);	
	}
	public void deleteAllRecordsForUsersWithUserTypeAndGivenProject(UUID superAppId, UUID appId, List<String> userIds,int userType, UUID projectId) {
		repository.deleteAllRecordsForUsersWithUserTypeAndGivenProject(superAppId, appId, userIds, userType, projectId);	
	}
	public void deleteAllRecordsForUsersAndGivenProject(UUID superAppId, UUID appId, List<String> userIds, List<Integer> userPriorities,  UUID projectId) {
		repository.deleteAllRecordsForUsersAndGivenProject(superAppId, appId, userIds, userPriorities, projectId);	
	}
	public void deleteAllRecordsForProjects(UUID superAppId, UUID appId, List<String> userIds, List<Integer> userPriorities,  List<UUID> projectIds) {
		System.out.println("Deleted User Project Mapping ::" + userIds + userPriorities + projectIds);
		repository.deleteAllRecordsForGivenProjects(superAppId, appId,  userIds, userPriorities, projectIds);	
		System.out.println("Deleted User Project Mapping ::" );
	}
	
	public int countOfProjectsAssignedToUsers(UUID superAppId, UUID appId) {
		
		List<UserProjectMapDBData> userProjectList = repository.countOfProjectsAssignedToUsers(superAppId, appId);
		Set<UUID> projIdsSet = new HashSet<UUID>();
		for (UserProjectMapDBData userProjecMap : userProjectList) {
			if(!userProjecMap.getUserId().equals(CommonConstants.DEFAULT_USER_ID)) {
				projIdsSet.add(userProjecMap.getProjectId());
			}
		}

		return projIdsSet.size();
	}
	public List<UserProjectMapping>  findUserProjectMappingByGivenUsers(UUID superAppId, UUID appId, List<String> userIds) {
		List<UserProjectMapping> data =  new ArrayList<>();
		List<UserProjectMapDBData> userProjectMapsData = repository.getAllAssignedProjectForApp(superAppId, appId, userIds);
		if(userProjectMapsData == null) {
    		return data;
		}
    	else {
    		data = createUserProjectListMapping(userProjectMapsData);
    		if(data != null && data.size() > 0 ) {
    			return data;
    		} else {
    			data = new  ArrayList<>();
    			return data;
    		}
    	}
		
	}
}
