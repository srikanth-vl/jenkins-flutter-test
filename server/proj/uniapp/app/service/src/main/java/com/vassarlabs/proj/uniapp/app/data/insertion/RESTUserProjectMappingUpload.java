package com.vassarlabs.proj.uniapp.app.data.insertion;

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
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.RefreshUserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.dsp.convertor.RawDataToDBObjectConvertor;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;
import com.vassarlabs.proj.uniapp.upload.pojo.ProjectUsersData;
import com.vassarlabs.proj.uniapp.upload.pojo.UserProjectMappingInput;

@Service
public class RESTUserProjectMappingUpload {
	
	@Autowired private UserProjectMapCrudService userProjectMappingCrudService;
	
	@Autowired private RawDataToDBObjectConvertor rawDataToDBObjectConvertor;
	
	@Autowired private DataUploadUtils dataUploadUtils;
	
	@Autowired private ProjectMasterDataCrudService masterDataCrudService;
	
	@Autowired private RefreshUserProjectMapping refreshUserProjectMapping;
	
	@Autowired protected IVLLogService logFactory;
	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}

	public Map<String, List<IErrorObject>> insertUserProjectMapping(UserProjectMappingInput userProjectMappingInput) throws IOException, InvalidInputException, InterruptedException, DSPException {
		
		UUID superAppId = userProjectMappingInput.getSuperAppId();
		UUID appId = userProjectMappingInput.getAppId();
		
		Map<String, List<IErrorObject>> externalIdToErrorList = new HashMap<>();
		
		int retryCount = 0;
		if(userProjectMappingInput == null || userProjectMappingInput.getProjectUserMappingInput() == null || userProjectMappingInput.getProjectUserMappingInput().isEmpty()) {
			throw new InvalidInputException();
		}
			try {
			Map<String, Map<Integer, UserProjectMapping>> userIdToTypeToProjectsMap = getUserProjectMappingMap(superAppId, appId);
			
			List<String> externalProjectIds = new ArrayList<>();
			externalProjectIds.addAll(userProjectMappingInput.getProjectUserMappingInput().stream().map(ProjectUsersData::getExternalProjectId).collect(Collectors.toSet()));
	
			Map<String, ProjectExternalInternalMapData> externalToInternalIdMap = rawDataToDBObjectConvertor.generateExternalToInternalIdMap(superAppId, appId, externalProjectIds);
			
			List<String> userIdsToDelete = new ArrayList<>();
			
			for(ProjectUsersData projectUserMapping : userProjectMappingInput.getProjectUserMappingInput()) { 
				
				String extProjectId = projectUserMapping.getExternalProjectId();
				
				List<IErrorObject> errorList = new ArrayList<>();
				
				if(externalToInternalIdMap.get(extProjectId) == null) {
					errorList.add(new ErrorObject("NO INTERNAL ID FOUND", IErrorObject.ERROR, "No project Internal ID found corresponding to external ID - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					externalIdToErrorList.put(projectUserMapping.getExternalProjectId(), errorList);
					continue;
				}
				UUID projectId = externalToInternalIdMap.get(extProjectId).getProjectId();
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
				if(masterData.getValue().equals(ProjectStates. DELETED.getValue())) {
					errorList.add(new ErrorObject("NO PROJECT ID FOUND", IErrorObject.ERROR, "Invalid ProjectId Found :: Project Deleted - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					externalIdToErrorList.put(projectUserMapping.getExternalProjectId(), errorList);
					continue;
				}
				if(projectUserMapping.getUserIdList()== null || projectUserMapping.getUserIdList().isEmpty() ) {
					errorList.add(new ErrorObject("NO PROJECT ID FOUND", IErrorObject.ERROR, "No user assinged for Project - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					externalIdToErrorList.put(projectUserMapping.getExternalProjectId(), errorList);
					continue;
				}
				dataUploadUtils.populateUserProjectMapping(superAppId, appId, projectId, projectUserMapping.getUserIdList(), projectUserMapping.getUserType(),
						userIdToTypeToProjectsMap, errorList, userIdsToDelete);
				
				if(!errorList.isEmpty()) {
					externalIdToErrorList.put(projectUserMapping.getExternalProjectId(), errorList);
				} 
			}
			
			dataUploadUtils.insertRecordsIntoDatabase(null, userIdToTypeToProjectsMap, externalToInternalIdMap);
			//refreshUserProjectMapping.refreshProjectIdsAssignedToDeafaultUser(superAppId, appId);
			userProjectMappingCrudService.deleteAllRecordsForUsersWithUserType(superAppId, appId, userIdsToDelete, UserPriorities.Default.getValue());
		} catch(CassandraReadTimeoutException | CassandraWriteTimeoutException | OperationTimedOutException | CassandraConnectionFailureException e) {
			if(retryCount++ >= CommonConstants.MAX_RETRIES) {
				logger.error("Max retries reached... Could not insert the data");
				throw new DSPException();
			}
			logger.debug("Sleeping for 3 seconds.. before retrying for " + retryCount + " time....");
			Thread.sleep(3000);
		}
		return externalIdToErrorList;
	}
	
	private Map<String,Map<Integer, UserProjectMapping>> getUserProjectMappingMap(UUID superAppId, UUID appId) throws IOException {
		Map<String,Map<Integer, UserProjectMapping>> userProjectMappingMap = new HashMap<>();
		List<UserProjectMapping> userProjectMappingFromDB = userProjectMappingCrudService.findUserProjectMappingByPartitionKey(superAppId, appId);
		if(userProjectMappingFromDB != null) {
			for(UserProjectMapping mapping : userProjectMappingFromDB) {
				if(!userProjectMappingMap.containsKey(mapping.getUserId())) {
					userProjectMappingMap.put(mapping.getUserId(), new HashMap<>());
				}
				Map<Integer, UserProjectMapping> userTypeToMappingMap = userProjectMappingMap.get(mapping.getUserId());
				userTypeToMappingMap.put(mapping.getUserType(), mapping);
			}
		}
		return userProjectMappingMap;
	}
}