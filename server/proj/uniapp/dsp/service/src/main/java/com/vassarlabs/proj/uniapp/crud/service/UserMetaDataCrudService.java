package com.vassarlabs.proj.uniapp.crud.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
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

import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.dsp.repository.UserMetaDataRepository;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class UserMetaDataCrudService {
	
	@Autowired UserMetaDataRepository repository;
	
	public void insertUserMetaData(UserDBMetaData data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertUserMetaData(List<UserDBMetaData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }
    
    public List<UserDBMetaData> findUserDataByPartitionKey(UUID superAppId, UserStates state) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return filterActiveInActiveUsers(repository.findByPartitionKey(superAppId), state);
    }
    
    public UserDBMetaData findUserDataByUserIdKey(UUID superAppId, String userId, UserStates state) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	UserDBMetaData user = repository.findByPrimaryKey(superAppId, userId);
    	if(user == null)
    		return null;
    	else {
    		return filterActiveInActiveUsers(Arrays.asList(user), state).stream().findFirst().orElse(null);
    	}
    }
    
    public Map<String, UserDBMetaData> getMetaDataForListOfUsers(UUID superAppId, List<String> userIds, UserStates state) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return filterActiveInActiveUsers(repository.getMetaDataForListOfUsers(superAppId, userIds), state).stream().collect(Collectors.toMap(UserDBMetaData::getUserId, Function.identity()));
    }
    
	public boolean updatePassword(UUID superAppId, String userId, String userExtId, String oldPassword, String newPassword)throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		
		UserDBMetaData userMetaData = repository.findByPrimaryKey(superAppId, userId);
		if(userMetaData.getPassword().equals(oldPassword)) {
			repository.updatePassword(superAppId, userId,userExtId, newPassword);
			return true;
		} else {
			return false;
		}
	}
	public void updateMapFiles(UUID superAppId, String userId, String userExtId, Map<UUID, String> mapfiles) {
			repository.updateMapFiles(superAppId, userId, userExtId, mapfiles);
	}

	public void deleteAllRecordsForASuperApp(UUID superAppId) {
		repository.deleteBySuperAppId(superAppId);
	}
	
	public String getExternalToInternalMappingFromMV(UUID superAppId, String userExtId) {
		UserDBMetaData dataFromDB =  repository.getExternalToInternalMappingFromMV(superAppId, userExtId);
		if(dataFromDB == null) {
			return null;
		}
		return dataFromDB.getUserId();
	}
	
	public boolean resetPassword(UUID superAppId, String userId, String userExtId,  String newPassword)throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
			repository.updatePassword(superAppId, userId, userExtId, newPassword);
			return true;	
	}
	
	public boolean setOtpObject(UUID superAppId, String userId, String userExtId, String newOtpObject)throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
			repository.updateOtp(superAppId, userId, userExtId, newOtpObject);
			return true;	
	}
	
	private List<UserDBMetaData> filterActiveInActiveUsers(List<UserDBMetaData> userMetadataList, UserStates activeFlag){

		List<UserDBMetaData> filteredUsers = new ArrayList<UserDBMetaData>();

		if (userMetadataList == null || userMetadataList.isEmpty()) 
			return filteredUsers;

		for (UserDBMetaData user : userMetadataList) {
			if(activeFlag == UserStates.INACTIVE  || user.isActive() == UserStates.ACTIVE.getValue()) {
				filteredUsers.add(user);
			}
		}
		return filteredUsers;
	}
}
