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

import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTokenData;
import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.dsp.repository.UserTokenDataRepository;

@Component
public class UserTokenDataCrudService {
	
	@Autowired private UserTokenDataRepository repository;
	@Autowired private ApplicationProperties properties;
	
	public void insertUserTokenData(UserTokenData data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertUserTokenData(List<UserTokenData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }
	
    public List<UserTokenData> findByPartitionKey(UUID superAppId, String userId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(superAppId, userId);
    }
    
    public UserTokenData findByPrimaryKey(UUID superAppId, String userId, UUID tokenId) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, IOException {
    	
    	String serverExpirationInterval = properties.getProperty("server-expiration-interval");

    	UserTokenData tokenData = repository.findByPrimaryKey(superAppId, userId, tokenId);
    	if(tokenData != null) {
    		tokenData.setInsertTs(tokenData.getInsertTs() == null ? 0 : tokenData.getInsertTs());
    		tokenData.setSyncTs(tokenData.getSyncTs() == null ? 0 : tokenData.getSyncTs());
    		tokenData.setTokenExpired(tokenData.getTokenExpired() == null ? 1 : tokenData.getTokenExpired());
    	}
    	else {
    		System.out.println("token is null" + tokenData  );
    		return null;
    	}
    	if(tokenData.getTokenExpired() == CommonConstants.TOKEN_EXPIRED) {
    		return tokenData;
    	}
    	if((System.currentTimeMillis() - tokenData.getInsertTs()) > Long.parseLong(serverExpirationInterval)) {
    		tokenData.setTokenExpired(CommonConstants.TOKEN_EXPIRED);
    		updateTokenExpiry(tokenData.getTokenExpired(), tokenData.getSuperAppId(), tokenData.getUserId(), tokenId);
    	}
    	return tokenData;
    }
    
    public void updateUserSyncTime( long syncTS, UUID superAppId, String userId, UUID tokenId) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.updateSyncTime(syncTS, superAppId, userId, tokenId);
    }
    
    public void updateTokenExpiry(int tokenExpired, UUID superAppId, String userId, UUID tokenId)  throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.updateTokenExpiry(tokenExpired, superAppId, userId, tokenId);
    }
    
    public void expireAllTokenForAUser(UUID superAppId, String userId)  throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	List<UserTokenData> userTokens = repository.findByPartitionKey(superAppId, userId);
    	if(userTokens == null || userTokens.isEmpty()) {
    		return;
    	}
    	for (UserTokenData userTokenData : userTokens) {
    		userTokenData.setTokenExpired(1);
    	}
    	repository.saveAll(userTokens);
    }
    
    public List<UserTokenData> findUsersLogInAtleastOnce(UUID superAppId, List<String> userIds) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findDistinctUsersLogIn(superAppId, userIds);
    }
}
