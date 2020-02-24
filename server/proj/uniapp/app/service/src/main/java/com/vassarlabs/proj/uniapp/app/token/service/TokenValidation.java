package com.vassarlabs.proj.uniapp.app.token.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.ApiRequestObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTokenData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.UserTokenDataCrudService;

@Component
@Aspect
public class TokenValidation {

	@Autowired UserTokenDataCrudService tokenDataCrudService;
	
	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@Before(" (execution(* com.vassarlabs.proj.uniapp.app.rootconfig.service..*(..)) "
//			+ " || execution(* com.vassarlabs.proj.uniapp.app.projectform.insert.service..*(..)) "
			+ " || execution(* com.vassarlabs.proj.uniapp.app.form.load..*(..)) "
			+ " || execution(* com.vassarlabs.proj.uniapp.app.project.listload..*(..)) "
			+ " || execution(* com.vassarlabs.proj.uniapp.login.service.LogoutService.*.*(..)) "
			+ " || execution(* com.vassarlabs.proj.uniapp.login.service.ChangePasswordService.*.*(..))) "
			+ "&& args(object)")
	public void validateToken(JoinPoint joinPoint, Object object) throws TokenNotFoundException, TokenExpiredException, CassandraConnectionFailureException, CassandraTypeMismatchException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraQuerySyntaxException, CassandraInternalException, IOException {

		logger.info("Inside Token Validation service");
		UserTokenData userTokenData = null;
		ApiRequestObject requestObject = (ApiRequestObject) object;
		IErrorObject errorObject = null;
		if(requestObject == null || requestObject.getTokenId() == null || requestObject.getTokenId().equals(UUIDUtils.getDefaultUUID())) {
            //No need to authenticate 
            return;
        }
		userTokenData = tokenDataCrudService.findByPrimaryKey(requestObject.getSuperAppId(), requestObject.getUserId(), requestObject.getTokenId());
		if(userTokenData == null) {
			errorObject = new ErrorObject("No Data Found", IErrorObject.INVALID_USER, "No data found for given token- Can't authorize further", -1);
			throw new TokenNotFoundException(errorObject);
		} else {
			if(userTokenData.getTokenExpired() == CommonConstants.TOKEN_EXPIRED) {
				errorObject = new ErrorObject("Token expired", IErrorObject.INVALID_USER, "Token expired for user- Can't authorize further", -1);
				throw new TokenExpiredException(errorObject);
			} 
		}
	}
}
