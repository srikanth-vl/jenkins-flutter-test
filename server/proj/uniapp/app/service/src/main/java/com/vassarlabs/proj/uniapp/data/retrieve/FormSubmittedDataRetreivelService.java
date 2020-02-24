package com.vassarlabs.proj.uniapp.data.retrieve;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.MediaDownloadRequestParams;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.api.IMediaDownloadService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.FormSubmittedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;


@Component
public class FormSubmittedDataRetreivelService {

	@Autowired private FormSubmittedDataCrudService formSubmittedDataCrudService;
	
	@Autowired private ProjectExternalToInternalMappingCrudService projExtToIntMapCrudService;

	@Autowired private IMediaDownloadService mediaDownloadService;

	@Autowired private IVLLogService logFactory;

	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	public Map<String, Map<UUID, Map<Long,Map<String,String>>>> getFormSubmitDataForSuperApp(UUID superapp, UUID app, int startDate, int endDate){
		
		List<ProjectExternalInternalMapData> projExtIntMapData = projExtToIntMapCrudService.findProjectExternalInternalMapDataByPartitionKey(superapp, app);
		Map<String, Map<UUID, Map<Long,Map<String,String>>>> keyValueDataMap = new HashMap<>();
		if(superapp == null || app == null) {
			logger.error("In getUserFormSubmitData() - Found NULL parameters : superAppId : " + superapp + " appId : " + app);
			return keyValueDataMap;
		}
		
		for (ProjectExternalInternalMapData projectMapData : projExtIntMapData) {
			Map<Long,Map<String,String>> keyValue = getUserFormSubmitSingleData(superapp, app, projectMapData.getProjectId(), 
					UUIDUtils.getDefaultUUID(), startDate, endDate);
			Map<UUID, Map<Long,Map<String,String>>> projIdTokeyValues = new HashMap<>();
			projIdTokeyValues.put(projectMapData.getProjectId(), keyValue);
			keyValueDataMap.put(projectMapData.getProjectExternalId(), projIdTokeyValues);
		}
		
		return keyValueDataMap;
	}

	public Map<Long,Map<String,String>> getUserFormSubmitSingleData(UUID superapp, UUID app, UUID projectId, UUID token, int startDate, 
			int endDate){

		List<FormSubmitData> formSubmitData = formSubmittedDataCrudService.findFormSubmittedDataByPartitionKey(superapp, app, projectId);
		Map<Long,Map<String,String>> keyValue = new HashMap<>();	
		if(superapp == null || app == null  || token == null || projectId == null) {
			logger.error("In getUserFormSubmitData() - Found NULL parameters : superAppId : " + superapp + " appId : " + app + " projectId : "
					+ projectId + " token : " + token);
			return keyValue;
		}
		
		for(FormSubmitData formData : formSubmitData) {
			int date = formData.getDate();
			if(date >= startDate && date < endDate
					&& formData.getValue() != null) {
				if(!keyValue.containsKey(formData.getTimestamp())) {
					keyValue.put(formData.getTimestamp(), new HashMap<String, String>());
				}
				Map<String,String> map = keyValue.get(formData.getTimestamp());
				map.put(formData.getKey(),formData.getValue());
				keyValue.put(formData.getTimestamp(), map);	
			}
		}
		
		return keyValue;
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
