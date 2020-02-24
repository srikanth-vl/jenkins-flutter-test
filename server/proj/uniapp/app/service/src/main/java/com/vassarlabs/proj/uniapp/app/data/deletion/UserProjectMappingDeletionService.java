package com.vassarlabs.proj.uniapp.app.data.deletion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.RefreshUserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.upload.pojo.ProjectUsersData;
import com.vassarlabs.proj.uniapp.upload.pojo.UserProjectMappingInput;

@Service
public class UserProjectMappingDeletionService {
	
	@Autowired private DataDeleteUtils dataDeleteUtils;
	
	@Autowired private ProjectMasterDataCrudService masterDataCrudService;
	
	@Autowired private UserProjectMapCrudService userProjectMapCrudService;
	@Autowired private RefreshUserProjectMapping refreshUserProjectMapping;
	@Autowired private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	
	@Autowired
	protected IVLLogService logFactory;

	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	public Map<String, List<IErrorObject>> deleteUserProjectMapping(UserProjectMappingInput userProjectMappingInput) throws IOException, InvalidInputException, DataDeletionException, InterruptedException {
		
		UUID superAppId = userProjectMappingInput.getSuperAppId();
		UUID appId = userProjectMappingInput.getAppId();
		int retryCount = 0 ;
		
		Map<String, List<IErrorObject>> externalIdToErrorList = new HashMap<>();
		try {
			if(userProjectMappingInput.getProjectUserMappingInput()== null || userProjectMappingInput.getProjectUserMappingInput().isEmpty()) {
				userProjectMapCrudService.deleteAllRecords(superAppId, appId);
				refreshUserProjectMapping.refreshProjectIdsAssignedToDeafaultUser(superAppId, appId);
				return externalIdToErrorList;
			}
			List<String> externalProjectIds = new ArrayList<>();
			externalProjectIds.addAll(userProjectMappingInput.getProjectUserMappingInput().stream().filter(p -> p.getExternalProjectId() != null).map(ProjectUsersData::getExternalProjectId).collect(Collectors.toSet()));
	
			Map<String, ProjectExternalInternalMapData> externalToInternalIdMap = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataForProjectExternalIds(superAppId, appId, externalProjectIds);
			
			List<String> userIdsToDelete = new ArrayList<>();
			
			for(ProjectUsersData projectUserMapping : userProjectMappingInput.getProjectUserMappingInput()) { 
	
				String extProjectId = projectUserMapping.getExternalProjectId();
				UUID projectId = null;
				userIdsToDelete =  projectUserMapping.getUserIdList();
				List<IErrorObject> errorList = new ArrayList<>(); 
				if(extProjectId != null && !extProjectId.isEmpty()) {
					if(externalToInternalIdMap.get(extProjectId) == null) {
						errorList.add(new ErrorObject("NO INTERNAL ID FOUND", IErrorObject.ERROR, "No project Internal ID found corresponding to external ID - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
						externalIdToErrorList.put(projectUserMapping.getExternalProjectId(), errorList);
						continue;
					}
					projectId = externalToInternalIdMap.get(extProjectId).getProjectId();
					if(projectId == null) {
						errorList.add(new ErrorObject("NO INTERNAL ID FOUND", IErrorObject.ERROR, "No project Internal ID found corresponding to external ID - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
						externalIdToErrorList.put(projectUserMapping.getExternalProjectId(), errorList);
						continue;
					}
					ProjectMasterData masterData = masterDataCrudService.findProjectMasterDataByPrimaryKey(superAppId, appId, projectId, CommonConstants.DEFAULT_DATE, MasterDataKeyNames.STATE_KEY, ProjectStates.ALL);
					if(masterData == null) {
						errorList.add(new ErrorObject("NO INTERNAL ID FOUND", IErrorObject.ERROR, "No project Internal ID found corresponding to external ID - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
						externalIdToErrorList.put(projectUserMapping.getExternalProjectId(), errorList);
						continue;
					}
				}
				//Delete user_project_mapping for given list of user
				System.out.println("Deleting userIds -" + userIdsToDelete);
				dataDeleteUtils.deleteUserProjectMapping(superAppId, appId, projectId, projectUserMapping.getUserIdList(), projectUserMapping.getUserType(), errorList, userIdsToDelete);
				if(!errorList.isEmpty()) {
					externalIdToErrorList.put(((extProjectId  == null || extProjectId.isEmpty())? CommonConstants.DEFAULT_KEY : extProjectId), errorList);
	
				}
			}
			refreshUserProjectMapping.refreshProjectIdsAssignedToDeafaultUser(superAppId, appId);
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
}