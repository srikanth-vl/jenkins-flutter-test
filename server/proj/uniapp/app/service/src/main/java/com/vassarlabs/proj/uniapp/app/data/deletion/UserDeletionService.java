package com.vassarlabs.proj.uniapp.app.data.deletion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.exceptions.OperationTimedOutException;
import com.vassarlabs.common.utils.err.DataDeletionException;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserTokenDataCrudService;
import com.vassarlabs.proj.uniapp.enums.UserStates;
import com.vassarlabs.proj.uniapp.upload.pojo.UserMetaDataList;
import com.vassarlabs.proj.uniapp.upload.pojo.UserMetaDataRequest;

@Service
public class UserDeletionService {
	
	@Autowired
	protected IVLLogService logFactory;

	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@Autowired private UserMetaDataCrudService userDataCrudService;
	@Autowired private UserTokenDataCrudService userTokenCrudService;
	@Autowired private UserProjectMapCrudService userProjectMapCrudService;
	@Autowired private ApplicationMetaDataCrudService applicationMetaDataCrudService;
	public Map<String, List<IErrorObject>> deleteUserMetaData(UserMetaDataRequest userMetaDataRequest) throws InvalidInputException, InterruptedException, DataDeletionException {
		Map<String, List<IErrorObject>> externalIdToErrorList = new HashMap<>();
		int retryCount = 0;
		if(userMetaDataRequest == null) {
			logger.error("userMetaDataRequest for deletion is null");
			throw new InvalidInputException("userMetaDataRequest for deletion is null");
		}
		List<UserDBMetaData> userDBDetailsData = null;
		try {
			// If no userIds are given, disable all the users for the super app
			if(userMetaDataRequest.getUserMetaData() == null || userMetaDataRequest.getUserMetaData().isEmpty()) {
				userDBDetailsData = userDataCrudService.findUserDataByPartitionKey(userMetaDataRequest.getSuperAppId(), UserStates.INACTIVE);
			} else {
				List<String> userIdList = userMetaDataRequest.getUserMetaData().stream().map(UserMetaDataList::getUserId).collect(Collectors.toList());
				userDBDetailsData = new ArrayList<>(userDataCrudService.getMetaDataForListOfUsers(userMetaDataRequest.getSuperAppId(), userIdList, UserStates.INACTIVE).values());
			}
			disableAllUsersOfASuperApp(userMetaDataRequest.getSuperAppId(), userDBDetailsData);
		} catch(CassandraReadTimeoutException | CassandraWriteTimeoutException | OperationTimedOutException | CassandraConnectionFailureException  e) {
			if(retryCount++ >= CommonConstants.MAX_RETRIES) {
				logger.error("Max retries reached... Could not insert the data");
				throw new DataDeletionException("Cassandra timeout error");
			}
			logger.debug("Sleeping for 3 seconds.. before retrying for " + retryCount + " time....");
			Thread.sleep(3000);
		}
		return externalIdToErrorList;
	}
	
	public void disableAllUsersOfASuperApp(UUID superAppId, List<UserDBMetaData> userDBDetailsData) {
		List<String> userIdsTodelete = new ArrayList<>();
		Set<UUID> appIds = new HashSet<>();
		for (UserDBMetaData user : userDBDetailsData) {
			if(!user.getUserId().equals(CommonConstants.DEFAULT_USER_ID)) {
				user.setActive(false);
				user.setInsertTs(System.currentTimeMillis());
				userDataCrudService.insertUserMetaData(user);
				if(user.getUserId() != null) {
					userTokenCrudService.expireAllTokenForAUser(superAppId, user.getUserId());
					userIdsTodelete.add(user.getUserId());
					if(user.getAppActions() != null && !user.getAppActions().isEmpty()) {
						appIds.addAll(user.getAppActions().keySet());
					}
				}
			}
		}
		for (UUID appId : appIds) {
			userProjectMapCrudService.deleteAllRecordsForGivenUsersForApp(superAppId, appId, userIdsTodelete);
		}
	}
}