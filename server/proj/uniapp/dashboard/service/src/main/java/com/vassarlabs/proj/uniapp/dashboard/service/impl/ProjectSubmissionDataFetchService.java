package com.vassarlabs.proj.uniapp.dashboard.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.MediaDownloadRequestParams;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.api.IMediaDownloadService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.FormSubmittedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectFormSubmissionDataRequestObject;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionData;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;

@Component
public class ProjectSubmissionDataFetchService {
	@Autowired private FormSubmittedDataCrudService formSubmittedDataCrudService;

	@Autowired private IMediaDownloadService mediaDownloadService;
	@Autowired private IVLLogService logFactory;
	@Autowired ProjectMasterDataCrudService projectMasterDataCrudService;

	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	public List<ProjectSubmissionData> getFormSubmittedData(UUID superapp, UUID app, List<UUID> projectIds){

		Map<UUID,List<FormSubmitData>> formSubmitData = formSubmittedDataCrudService.findFormSubmittedDataForListOfProjects(superapp, app, projectIds);
		List<ProjectSubmissionData> submissionData =  new ArrayList<ProjectSubmissionData>();;
		Map<UUID,Map<Long,Map<String,String>>> projectIdTokeyValue = new HashMap<>();	
		if(superapp == null || app == null  ||  projectIds == null || projectIds.isEmpty()) {
			logger.error("In getUserFormSubmitData() - Found NULL parameters : superAppId : " + superapp + " appId : " + app + " projectId : "
					+ projectIds + " token : ");
			return submissionData;
		}
		
		for (UUID  projectId : formSubmitData.keySet()) {
			Map<Long, Map<String,String>> keyToValue = new HashMap<>();
			for(FormSubmitData formData : formSubmitData.get(projectId)) {
					if(!keyToValue.containsKey(formData.getTimestamp())) {
						keyToValue.put(formData.getTimestamp(), new HashMap<String, String>());
						Map<String,String> map = keyToValue.get(formData.getTimestamp());
						map.put("user_id",formData.getUserId());
						keyToValue.put(formData.getTimestamp(), map);
					}
					Map<String,String> map = keyToValue.get(formData.getTimestamp());
					map.put(formData.getKey(),formData.getValue());
					keyToValue.put(formData.getTimestamp(), map);	
				
			}
			projectIdTokeyValue.put(projectId, keyToValue);
			
		}
		for (UUID  projectId : projectIdTokeyValue.keySet()) {
				Map<Long, Map<String,String>> keyToValue = projectIdTokeyValue.get(projectId);
				
				Map<String,ProjectMasterData> keyToMasterData = projectMasterDataCrudService.getKeyToTargetValue(superapp, app, projectId, ProjectStates.ALL);
				String projectName = keyToMasterData.get(MasterDataKeyNames.PROJ_NAME_KEY) != null ? keyToMasterData.get(MasterDataKeyNames.PROJ_NAME_KEY).getValue() : CommonConstants.NA;
				for (Long timestamp : keyToValue.keySet()) {
					ProjectSubmissionData data = new ProjectSubmissionData();
					data.setProjectId(projectId);
					data.setSubmittedData(keyToValue.get(timestamp));
					List<String> getDateTimeString = StringUtils.getStringListFromDelimitter(" ", DateUtils.getTimeStringFromTimeInMillis(timestamp));
					String date  = getDateTimeString.get(0);
					String time  = getDateTimeString.get(1);
					data.setDate(date);
					data.setTime(time);
					data.setUserId(keyToValue.get(timestamp).get("user_id"));
					data.setProjectName(projectName);
					submissionData.add(data);
					
				}
		}
		
		
		return submissionData;
	}

	public FormMediaValue getImageForTheData(UUID superappId,UUID appId, UUID projectId , UUID token ,String dataType , UUID imageUid , String userId) throws IOException {

		MediaDownloadRequestParams requestParams = new MediaDownloadRequestParams(projectId, dataType ,imageUid);	
		requestParams.setSuperAppId(superappId);
		requestParams.setAppId(appId);
		requestParams.setTokenId(token);
		requestParams.setUserId(userId);
		ServiceOutputObject serviceOutputObject = mediaDownloadService.downloadMedia(requestParams);
		FormMediaValue formValue = (FormMediaValue) serviceOutputObject.getOutputMap().get(CommonConstants.MEDIA);
		return  formValue;
	}

}
