package com.vassarlabs.proj.uniapp.app.project.listload;
/**
 * Returns all projects assigned to user -> last updated Time on server
 * @author nidhi
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.ProjectListRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.IProjectListDataService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.ProjectListConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.APITypes;

@Component("ProjectListWithTimeService")
public class ProjectListWithTimeService 
	implements IProjectListDataService {
	
	@Autowired private IVLLogService logFactory;
	private IVLLogger logger;
	private ObjectMapper objectMapper = new ObjectMapper();
	@Autowired private UserProjectMapCrudService userProjMappingData;
	@Autowired private DataRetrievalService dataRetreivalService;
	
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@Override
	public ServiceOutputObject getProjectListConfig(ProjectListRequestObject projListRequest)
			throws TokenNotFoundException, TokenExpiredException, IOException, DataNotFoundException,
			ValidationException {
		
		Map<String, Object> output = new HashMap<>();
		if (projListRequest == null) {
			logger.error("ProjectListRequestObject is NULL");
			return null;
		}
		
		UUID superAppId = projListRequest.getSuperAppId();
		UUID appId = projListRequest.getAppId();
		String userId = projListRequest.getUserId();
		UUID token = projListRequest.getTokenId();
		List<UUID> projectIdList = projListRequest.getProjectIdList();
		if(projectIdList == null) {
			// Return all project Ids assigned to the user
			List<UserProjectMapping> userProjectData = userProjMappingData.findUserProjectMappingByUserId(superAppId, appId, userId);
			if (userProjectData == null) {
				logger.error("No project found for user: " + projListRequest.getUserId());
				throw new DataNotFoundException("No project found for user: " + projListRequest.getUserId());
			}
			projectIdList = userProjectData.stream().flatMap(e -> e.getProjectList().stream()).collect(Collectors.toList());
		} 
		Map<UUID, Long> projectIdToLastSyncTS = new HashMap<>();
		// if projectIdList.isEmpty() : user have no project assigned, return empty list
		if(!projectIdList.isEmpty())  {
			// Get last sync TS for each of the projects :: Last Sync TS = Max(Last Project Submission Time, File Upload Time)
			projectIdToLastSyncTS = dataRetreivalService.getLastUpdatedTSforProjectList(superAppId, appId, projectIdList);
		}
		output.put(ProjectListConstants.PROJECTS, projectIdToLastSyncTS);
		long trackingTS = System.currentTimeMillis();
		UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, token, APITypes.PROJECT_LIST_WITH_TS,
				ServiceNamesConstants.PROJECT_LIST_CONFIG_NAME_1, objectMapper.writeValueAsString(projListRequest), null, true, trackingTS);
		output.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
		ServiceOutputObject outputObject = new ServiceOutputObject(output, trackingObject, true);
		return outputObject;
	}

}
