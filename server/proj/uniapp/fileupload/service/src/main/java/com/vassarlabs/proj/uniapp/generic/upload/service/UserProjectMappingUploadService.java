package com.vassarlabs.proj.uniapp.generic.upload.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.filereader.service.api.ICSVFileReaderService;
import com.vassarlabs.common.fileupload.event.api.IFileUploadEvent;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;
import com.vassarlabs.common.fileupload.pojo.impl.FileUploadResult;
import com.vassarlabs.common.filewriter.pojo.api.IFileWriterDetails;
import com.vassarlabs.common.filewriter.service.api.ICSVFileWriterService;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.ObjectNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.RefreshUserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;

@Component
public class UserProjectMappingUploadService
	extends UniAppFileUpload {
	
	@Autowired private ICSVFileReaderService fileReaderService;
	@Autowired private ICSVFileWriterService fileWriterService;
	@Autowired private UserProjectMapCrudService userProjectMappingCrudService;
	@Autowired private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	@Autowired private UserProjectMapUtils userProjMappingUtils;
	@Autowired private RefreshUserProjectMapping refreshUserProjectMapping;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired protected IVLLogService logFactory;

	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@EventListener
	public <E> void fileUploadEvent(IFileUploadEvent<E> fileUploadEvent) throws DSPException, ObjectNotFoundException, InvalidInputException, IOException, IllegalArgumentException, IllegalAccessException {
		if(fileUploadEvent.getFileUploadDetails().getClassName().contains("UserMappingInfo")) {
		try {	
			Map<E, List<IErrorObject>> rowToErrorListMap = fileUploadEvent.getFileUploadResult().getDataToErrorListMap();
			List<E> list = fileUploadEvent.getDataList();
			List<UserProjectMapping> insertList = new ArrayList<>();
			Properties properties = fileUploadEvent.getFileUploadDetails().getProperties();
			String userConfig = properties.getProperty(CommonConstants.JSON_CONFIG);
			JsonNode userMappingNode = objectMapper.readTree(userConfig);
			UUID superAppId = getSuperAppId(userConfig);
			UUID appId = getAppId(userConfig);
			Map<String, Map<Integer, UserProjectMapping>> userProjectMappingMap = userProjMappingUtils.getUserProjectMappingMap(superAppId, appId);
			List<String> externalProjIds = getAllProjectExternalIds(userMappingNode.get(IFileUploadConstants.UserProjectMappingUploadConstants.MAPPING), fileUploadEvent.getDataList());
			Map<String, ProjectExternalInternalMapData> externalToInternalIdMap = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataForProjectExternalIds(superAppId, appId, externalProjIds);
			for(E object : list) {
				List<IErrorObject> errors = userProjMappingUtils.populateUserProjectMappingData(object, userProjectMappingMap, userMappingNode.get(IFileUploadConstants.UserProjectMappingUploadConstants.MAPPING), superAppId, appId, externalToInternalIdMap, null);
				boolean result = errors.stream().anyMatch(x->x.getRowUploadStatus().equals(IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				if(result) {
					rowToErrorListMap.put(object, errors);
				} 
			}
			for(String userId : userProjectMappingMap.keySet()) {
				for(Integer userType : userProjectMappingMap.get(userId).keySet()) {
					insertList.add(userProjectMappingMap.get(userId).get(userType));
				}
			}
			userProjectMappingCrudService.insertUserProjectMappingData(insertList);
			if(!rowToErrorListMap.isEmpty()) {
				IFileWriterDetails fileWriterDetails = createFileWriterObject(fileUploadEvent);
				fileWriterService.writeCSVFile(fileWriterDetails, rowToErrorListMap);
			}
			consolidateCountOfRecords(rowToErrorListMap.size(), insertList.size(), fileUploadEvent.getDataList().size(), fileUploadEvent.getFileUploadDetails(), fileUploadEvent.getFileUploadResult());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
	}

	private <E> List<String> getAllProjectExternalIds(JsonNode userMappingNode,  List<E> dataList) throws IllegalArgumentException, IllegalAccessException {
		List<String> externalIds = new ArrayList<>();
		for(E mappingObject : dataList) {
			for(JsonNode objectNode : userMappingNode) {
				for (Field f : mappingObject.getClass().getDeclaredFields()) {
					f.setAccessible(true);
					if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.UserProjectMappingUploadConstants.PROJECT_ID, objectNode))) {
						String userExtId = String.valueOf(f.get(mappingObject));
						externalIds.add(userExtId);
					}
				}
			}
		}
		return externalIds;
	}

	public void uploadFile(IFileUploadDetails fileDetails) throws InvalidInputException, IOException {
		IFileUploadResult<?> fileUploadResult = new FileUploadResult<>();
		logger.debug("Starting Upload for file - " + fileDetails.getFileName());
		long startTS = System.currentTimeMillis();
		String jsonStringNode = fileDetails.getProperties().getProperty(CommonConstants.JSON_CONFIG);
		String uploadType = getUploadType(jsonStringNode);
		UUID superAppId = getSuperAppId(jsonStringNode);
		UUID appId = getAppId(jsonStringNode);
		userProjMappingUtils.processQueryForUploadType(uploadType, superAppId, appId);
		fileReaderService.readCSVFile(fileDetails, fileUploadResult);
		printResult(fileUploadResult);
		refreshUserProjectMapping.refreshProjectIdsAssignedToDeafaultUser(superAppId, appId);
		logger.debug("Total Time taken for uploading -" + fileDetails.getFileName() + " : " + (System.currentTimeMillis() - startTS));
	}
}
