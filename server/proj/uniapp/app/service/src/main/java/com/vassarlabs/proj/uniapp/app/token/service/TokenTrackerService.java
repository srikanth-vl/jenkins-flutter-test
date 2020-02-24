package com.vassarlabs.proj.uniapp.app.token.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.crud.service.UserTokenDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserTrackingDataCrudService;

@Component
@Aspect
public class TokenTrackerService {
	
	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@Autowired UserTrackingDataCrudService trackingCrudService;
	@Autowired UserTokenDataCrudService tokenDataCrudService;
	
	@AfterReturning(value="execution(* com.vassarlabs.proj.uniapp.app..*(..))", returning="outputObject")
	public void trackData(JoinPoint joinPoint, ServiceOutputObject outputObject) {
		if(outputObject == null) {
			logger.error("In TokenTrackerService :: trackData()- Got NULL output object");
			return;
		}
		UserTrackingObject trackingObject = outputObject.getTrackingObject();
		logger.info("In trackData() - Updating Last Sync Time of the User and inserting information into user tracking table"
				+ " for superAppId -" + trackingObject.getSuperAppId() + " app Id - " + trackingObject.getAppId() + " userId - " + trackingObject.getUserId() + " apiType - " + 
				trackingObject.getApiType() + " request Type - " + trackingObject.getRequestObj() + " Token Id - " + trackingObject.getTokenId());
		if(!trackingObject.getTokenId().equals(UUIDUtils.getDefaultUUID())) {
			tokenDataCrudService.updateUserSyncTime(System.currentTimeMillis(), trackingObject.getSuperAppId(), trackingObject.getUserId(), trackingObject.getTokenId());
		}
		trackingCrudService.insertUserTrackingInformation(trackingObject.getSuperAppId(), trackingObject.getAppId(), trackingObject.getUserId(), trackingObject.getTokenId(), trackingObject.getInsertTimeStamp(),
				trackingObject.getApiType(), trackingObject.getApi(), trackingObject.getRequestObj(),  trackingObject.isRequestSuccessful(), trackingObject.getErrorsList());
	}
	
	@AfterReturning(value="execution(* com.vassarlabs.proj.uniapp.app..*(..))", returning="outputObjectList")
	public void trackData(JoinPoint joinPoint, List<ServiceOutputObject> outputObjectList) {
		if(outputObjectList == null)
			return;
		int index = 0;
		for(ServiceOutputObject outputObject : outputObjectList) {
			index ++;
			UserTrackingObject trackingObject = outputObject.getTrackingObject();
			logger.info("In trackData() - Updating Last Sync Time of the User and inserting information into user tracking table"
					+ " for superAppId -" + trackingObject.getSuperAppId() + " app Id - " + trackingObject.getAppId() + " userId - " + trackingObject.getUserId() + " apiType - " + 
					trackingObject.getApiType() + " request Type - " + trackingObject.getRequestObj() + " Token Id - " + trackingObject.getTokenId());
	
			trackingCrudService.insertUserTrackingInformation(trackingObject.getSuperAppId(), trackingObject.getAppId(), trackingObject.getUserId(), trackingObject.getTokenId(), trackingObject.getInsertTimeStamp(),
					trackingObject.getApiType(), trackingObject.getApi(), trackingObject.getRequestObj(), trackingObject.isRequestSuccessful(), trackingObject.getErrorsList());
			if(index == outputObjectList.size()) {
				tokenDataCrudService.updateUserSyncTime(System.currentTimeMillis(), trackingObject.getSuperAppId(), trackingObject.getUserId(), trackingObject.getTokenId());
			}
		}
	}
}
