package com.vassarlabs.proj.uniapp.crud.service;

import java.io.IOException;
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

import com.vassarlabs.proj.uniapp.application.dsp.pojo.DashboardTokenData;
import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.dsp.repository.DashboardTokenDataRepository;

@Component
public class DashboardTokenDataCrudService {
	
	@Autowired DashboardTokenDataRepository repository;
	@Autowired private ApplicationProperties properties;
	
	public void insertUserTokenData(DashboardTokenData data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertUserTokenData(List<DashboardTokenData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }
	
    public List<DashboardTokenData> findByPartitionKey(String userId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(userId);
    }
	
    public DashboardTokenData findByPrimaryKey(String userId, UUID tokenId) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, IOException {
    	
//    	String serverExpirationInterval = properties.getProperty("server-expiration-interval");

    	DashboardTokenData tokenData = repository.findByPrimaryKey(userId, tokenId);
    	if(tokenData.getTokenExpired() == CommonConstants.TOKEN_EXPIRED) {
    		return tokenData;
    	}
//    	if((System.currentTimeMillis() - tokenData.getInsertTs()) > Long.parseLong(serverExpirationInterval)) {
//    		tokenData.setTokenExpired(CommonConstants.TOKEN_EXPIRED);
//    		updateTokenExpiry(tokenData.getTokenExpired(), tokenData.getUserId(), tokenId);
//    	}
    	return tokenData;
    }
    
    public void updateUserSyncTime( long syncTS, String userId, UUID tokenId) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.updateSyncTime(syncTS, userId, tokenId);
    }
    
    public void updateTokenExpiry(int tokenExpired, String userId, UUID tokenId)  throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.updateTokenExpiry(tokenExpired, userId, tokenId);
    }
    
    public void expireAllTokenForAUser(String userId)  throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	List<DashboardTokenData> userTokens = repository.findByPartitionKey(userId);
    	if(userTokens == null || userTokens.isEmpty()) {
    		return;
    	}
    	for (DashboardTokenData userTokenData : userTokens) {
    		userTokenData.setTokenExpired(1);
    	}
    	repository.saveAll(userTokens);
    }
    
}
