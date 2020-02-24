package com.vassarlabs.proj.uniapp.app.login.service;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.ApiRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.ILogoutService;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.UserTokenDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;

@Component
public class LogoutService 
	implements ILogoutService {

	@Autowired private UserTokenDataCrudService tokenDataCrudService;
	
	@Override
	public ServiceOutputObject logout(ApiRequestObject requestObject)  throws CassandraConnectionFailureException, CassandraReadTimeoutException,
    	CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    	TokenNotFoundException, TokenExpiredException, JsonProcessingException {
		
		UUID appId = requestObject.getSuperAppId();
		tokenDataCrudService.updateTokenExpiry(CommonConstants.TOKEN_EXPIRED, requestObject.getSuperAppId(), requestObject.getUserId(), 
				requestObject.getTokenId());
		long trackingTS = System.currentTimeMillis();
		UserTrackingObject trackingObject = new UserTrackingObject(requestObject.getSuperAppId(), appId, requestObject.getUserId(), requestObject.getTokenId(), APITypes.LOGOUT,
				ServiceNamesConstants.LOGOUT, new ObjectMapper().writeValueAsString(requestObject), null, true,trackingTS);
		ServiceOutputObject outputObject = new ServiceOutputObject();
		outputObject.setSuccessful(true);
		outputObject.setTrackingObject(trackingObject);
		return outputObject;
	}
}
