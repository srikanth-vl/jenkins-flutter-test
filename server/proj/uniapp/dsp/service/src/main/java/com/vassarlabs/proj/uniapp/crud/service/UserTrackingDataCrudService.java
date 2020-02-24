package com.vassarlabs.proj.uniapp.crud.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTrackingData;
import com.vassarlabs.proj.uniapp.dsp.repository.UserTrackingDataRepository;
import com.vassarlabs.proj.uniapp.enums.APITypes;

@Component
public class UserTrackingDataCrudService {
	
	@Autowired private UserTrackingDataRepository repository;
	
	public void insertUserTrackingData(UserTrackingData data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
	public void insertUserTokenData(List<UserTrackingData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }
	
    public List<UserTrackingData> findByPartitionKey(UUID superAppId, UUID appId, String userId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(superAppId, appId, userId);
    }
    
    public UserTrackingData findByPrimaryKey(UUID superAppId, UUID appId, String userId, long timeStamp, String apiType, UUID tokenId) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPrimaryKey(superAppId, appId, userId, timeStamp, apiType, tokenId);
    }
    
    public void insertUserTrackingInformation(UUID superAppId, UUID appId, String userId, UUID token, long timeStamp, APITypes apiType, String api, 
    	String requestObject, boolean isSuccessfulRequest, List<String> errorMessages) throws CassandraConnectionFailureException, 
    	CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	
    	UserTrackingData trackingData = new UserTrackingData();
    	trackingData.setSuperAppId(superAppId);
    	trackingData.setAppId(appId);
    	trackingData.setUserId(userId);
    	trackingData.setTokenId(token);
    	trackingData.setTimeStamp(timeStamp);
    	trackingData.setApiType(apiType.getValue());
    	trackingData.setApi(api);
    	trackingData.setRequestObj(requestObject);
    	trackingData.setRequestSuccessful(isSuccessfulRequest);
    	trackingData.setInsertTs(System.currentTimeMillis());
    	trackingData.setErrors(errorMessages);
    	insertUserTrackingData(trackingData);
	}
    
    public List<UserTrackingData> findTransactionLogforUserByApiType(UUID superAppId, UUID appId, String userId, String apiType, int size, long timestamp) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findTransactionLogforUserByApiType(superAppId, appId, userId, apiType, size, timestamp);
    }
    public List<Long>  findMinimumTimestamp(UUID superAppId, UUID appId, String userId, String apiType, int size, int pageno) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	int noOfRows  = size*(pageno-1);
    	return repository.findTimestamps(superAppId, appId, userId, apiType, noOfRows);
    	
    }
    public int findCountOfRows(UUID superAppId, UUID appId, String userId, String apiType) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findCountOfRows(superAppId, appId, userId, apiType);
    	
    }
    
    public List<UserTrackingData> findTransactionLogforUsersByApiType(UUID superAppId, UUID appId, List<String> userId, String apiType, long timestamp) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	//Run in batches
    	return repository.findTransactionLogforUsersByApiType(superAppId, appId, userId, apiType, timestamp);
    }
}
