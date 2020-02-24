package com.vassarlabs.proj.uniapp.app.data.deletion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.exceptions.OperationTimedOutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataDeletionException;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.app.data.utilities.ProjectDataUtils;
import com.vassarlabs.proj.uniapp.app.update.projects.status.service.ProjectDisableService;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.RefreshUserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;

@Service
public class ProjectDeletionService {

	@Autowired private RefreshUserProjectMapping refreshUserProjectMapping;
	@Autowired private ProjectDisableService  projectDisableUtility;
	@Autowired private DataDeleteUtils dataDeleteUtils;
	@Autowired private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	@Autowired
	protected IVLLogService logFactory;

	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}

	ObjectMapper objectMapper = new ObjectMapper();

	public Map<String, List<IErrorObject>> execute(AppFormDataSubmittedList appFormSubmittedDataList) throws InvalidInputException, IOException, DataDeletionException, InterruptedException {
		
		UUID superAppId = appFormSubmittedDataList.getSuperAppId();
		UUID appId = appFormSubmittedDataList.getAppId();
		Map<String, List<IErrorObject>> externalIdToErrorList = new HashMap<>();
		List<AppFormData> appFormDataList = appFormSubmittedDataList.getAppFormDataList();
		int retryCount = 0;
		try {
			if(appFormDataList == null || appFormDataList.isEmpty()) {
				projectDisableUtility.disableAllProjectForApp(superAppId, appId);
				return externalIdToErrorList;
			}
			List<String> externalProjectIds = ProjectDataUtils.getAllExternalProjectIds(appFormDataList);
			
			Map<String, ProjectExternalInternalMapData> externalToInternalIdMap = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataForProjectExternalIds(superAppId, appId, externalProjectIds);
			int indexCount = 0;
			
			List<UUID> projectIdsToDelete =  new ArrayList<>();
			for(AppFormData appFormData : appFormDataList) {
				indexCount++;
				List<IErrorObject> errorList = new ArrayList<>(); 
				
				List<FormFieldValues> fieldValuesList = appFormData.getFormFieldValuesList();
				
				String externalProjectId = fieldValuesList.stream().findFirst().filter(p -> p.getKey().equalsIgnoreCase(MasterDataKeyNames.EXTERNAL_PROJECT_ID)).get().getValue();
				
				if(externalProjectId == null || externalProjectId.isEmpty()) {
					logger.error("No external Id found for project");
					createErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "No project external Id found!", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE, appFormData, indexCount, errorList);
					externalIdToErrorList.put(CommonConstants.DEFAULT_KEY, errorList);
					continue;
				}
				UUID projectId = null;
				ProjectExternalInternalMapData externalToInernalProjectData = externalToInternalIdMap.get(externalProjectId);
				projectId = externalToInernalProjectData != null ? externalToInernalProjectData.getProjectId() : null;
				if(projectId == null) {
					logger.error("No internal Id found for project" + externalProjectId);
					createErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "No project internal Id found!", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE, appFormData, indexCount, errorList);
					externalIdToErrorList.put(externalProjectId, errorList);
					continue;
				}
				
				projectIdsToDelete.add(projectId);
				if(!errorList.isEmpty()) {
					externalIdToErrorList.put(externalProjectId, errorList);
				}
			}
			if(!projectIdsToDelete.isEmpty()) {
				
				// delete Projects
				projectDisableUtility.disableProject(superAppId, appId, projectIdsToDelete);
	
				// delete UserProjectMapping for deleted projects
				
				dataDeleteUtils.deleteUserProjectMappingForGivenProjects(superAppId, appId, projectIdsToDelete);
	
				// refresh project assigned to default user
				refreshUserProjectMapping.refreshProjectIdsAssignedToDeafaultUser(superAppId, appId);
			}
		} catch(CassandraReadTimeoutException | CassandraWriteTimeoutException | OperationTimedOutException | CassandraConnectionFailureException e) {
			if(retryCount++ >= CommonConstants.MAX_RETRIES) {
				logger.error("Max retries reached... Could not insert the data");
				throw new DataDeletionException("Cassandra timeout error");
			}
			logger.debug("Sleeping for 3 seconds.. before retrying for " + retryCount + " time....");
			Thread.sleep(3000);
		}
		
		return externalIdToErrorList;
	}

	private void createErrorObject(String errorCodeStr, int errorCode, String errorMessage,
			String uploadStatus, AppFormData appFormData, int indexCount, List<IErrorObject> errorList) throws JsonProcessingException {
		IErrorObject errorObject = new ErrorObject();
		errorObject.setErrorCode(errorCode);
		errorObject.setErrorMessage(errorMessage);
		errorObject.setErrorType(errorCodeStr);
		errorObject.setRowUploadStatus(uploadStatus);
		errorObject.setRowData(objectMapper.writeValueAsString(appFormData));
		errorObject.setLineNo(indexCount);
		errorList.add(errorObject);
	}

	
}