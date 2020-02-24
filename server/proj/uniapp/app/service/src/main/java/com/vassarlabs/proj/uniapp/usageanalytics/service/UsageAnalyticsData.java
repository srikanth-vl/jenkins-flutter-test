package com.vassarlabs.proj.uniapp.usageanalytics.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.InvalidInputParamException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.prod.messaging.email.impl.EmailMessagingServiceImpl;
import com.vassarlabs.prod.messaging.model.Message;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTrackingData;
import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserTrackingDataCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class UsageAnalyticsData {

	@Autowired
	protected IVLLogService logFactory;
	protected IVLLogger logger;
	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Autowired SuperAppDataCrudService superAppDataCrudService;
	@Autowired ApplicationMetaDataCrudService appMetaDataCrudService;
	@Autowired ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	@Autowired DataRetrievalService dataRetrievalService;
	@Autowired UserProjectMapCrudService userProjectMapCrudService;
	@Autowired UserTrackingDataCrudService userTrackingCrudService;
	@Autowired FieldMetaDataCrudService fieldMetaDataCrudService;
	@Autowired UserMetaDataCrudService userMetaDataCrudService;
	@Autowired ApplicationProperties properties;
	@Autowired EmailMessagingServiceImpl emailMessagingServiceImpl;

	ObjectMapper objectMapper = new ObjectMapper();

	public void getUsageAnalyticsExcelData() throws IOException {

		Iterable<SuperApplicationData> superAppList = superAppDataCrudService.getAllSuperAppData();
		if(superAppList == null) {
			logger.info("Super app List data not available");
		}
		
		List<String> superAppFilePaths = new ArrayList<>();

		for(SuperApplicationData superAppData : superAppList) {
			if(superAppData == null) {
				logger.info("Super app is not present");
				continue;
			}

			String filePath = properties.getProperty("excel_filepath");
			UUID superAppId = superAppData.getSuperAppId();
			filePath = filePath + "/" + superAppData.getName() + ".xls";
			superAppFilePaths.add(filePath);
			FileOutputStream file = new FileOutputStream(filePath);
			XSSFWorkbook workbook = new XSSFWorkbook();
			Map<UUID, Integer> submissionCountForLast24Hrs = new HashMap<>();
			Map<UUID, Integer> submissionCountForLast7Days = new HashMap<>();
			List<ApplicationMetaData> applicationMetaDataList = appMetaDataCrudService.getApplicationMetaDataForSuperApp(superAppId);

			if(applicationMetaDataList == null || applicationMetaDataList.isEmpty()) {
				logger.info("Application meta data list not present for super app "+ superAppId);
				continue;
			}

			for(ApplicationMetaData applicationMetaData : applicationMetaDataList) {
				if(applicationMetaData == null) {
					logger.info("Super app is not present");
					continue;
				}

				UUID applicationId = applicationMetaData.getAppId();
				String applicationConfigData = applicationMetaData.getConfigData();
				Map<String, Object> applicationConfigDataMap =  objectMapper.readValue(applicationConfigData, new TypeReference<Map<String, Object>>(){});
				String applicationName = applicationId.toString();
				if(applicationConfigDataMap != null) {
					applicationName = applicationConfigDataMap.get(CommonConstants.APP_CONFIG_DATA_KEY_NAME).toString();
				}
				XSSFSheet worksheet = workbook.createSheet(applicationName);

				List<ProjectExternalInternalMapData> projectExternalInternalMapDataList = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataByPartitionKey(superAppId, applicationId);
				List<UUID> projectIdList = projectExternalInternalMapDataList.stream()
						.map(ProjectExternalInternalMapData::getProjectId)
						.collect(Collectors.toList());

				Map<UUID, Map<String, String>> projectValues = new HashMap<>();
				try {
					projectValues = dataRetrievalService.getValueForAProject(superAppId, applicationId, projectIdList, ProjectStates.ALL);
				} catch (DataNotFoundException e) {
					logger.info("Project data not available for application "+ applicationId+ " and super app "+ superAppId);
				}

				List<UserProjectMapping> userProjectMapping = userProjectMapCrudService.findUserProjectMappingByPartitionKey(superAppId, applicationId);
				List<String> userIdList = userProjectMapping.stream()
						.map(UserProjectMapping::getUserId)
						.collect(Collectors.toList());
				Map<UUID, Map<String, UserDBMetaData>> projectUserMappingData = getProjectUserMappingData(userProjectMapping, userIdList, superAppId,
																				applicationId, UserStates.ACTIVE);
				long currentTS = System.currentTimeMillis();
				List<UserTrackingData> userTrackingDataForLast24Hrs = userTrackingCrudService.findTransactionLogforUsersByApiType(superAppId,
						applicationId, userIdList, APITypes.SUBMIT.getValue(), DateUtils.getPreviousDaySameTimeTS(currentTS));
				List<UserTrackingData> userTrackingDataForLast7Days = userTrackingCrudService.findTransactionLogforUsersByApiType(superAppId,
						applicationId, userIdList, APITypes.SUBMIT.getValue(), DateUtils.getLast7DaysTs(currentTS));
				updateSubmissionCountForProjects(submissionCountForLast24Hrs, userTrackingDataForLast24Hrs);
				updateSubmissionCountForProjects(submissionCountForLast7Days, userTrackingDataForLast7Days);

				projectIdList.add(UUIDUtils.getDefaultUUID());
				List<FieldMetaData> fieldMetaDataList = fieldMetaDataCrudService.findLatestFieldMetaDataForProjects(superAppId,
						applicationId, projectIdList,FormTypes.UPDATE.getValue());
				// TODO: Use streams 
				Map<String, Set<String>> projectToFieldMap = new HashMap<>();

				for(FieldMetaData fieldMetaData: fieldMetaDataList) {
					if(fieldMetaData == null) {
						logger.info("Field meta data is not present");
						continue;
					}
					String labelName = fieldMetaData.getLabelName();
					String key = fieldMetaData.getKey();
					if(projectToFieldMap.get(labelName) == null) {
						if(labelName != null && !labelName.equals(CommonConstants.EMPTY_STRING)) {
							Set<String> keysList = new HashSet<>();
							keysList.add(key);
							projectToFieldMap.put(labelName, keysList);
						}
					}
					else {
						Set<String> keysList = projectToFieldMap.get(labelName);
						keysList.add(key);
						projectToFieldMap.put(labelName, keysList);
					}
				}

				int rowId = 0;
				XSSFRow mainHeaderRow = worksheet.createRow(rowId++);
				Set<String> fileHeader = projectToFieldMap.keySet();
				int cellId = 0;
				Cell cell;
				for(String header : fileHeader) {
					cell = mainHeaderRow.createCell(cellId++);
					cell.setCellValue(header);
				}
				cell = mainHeaderRow.createCell(cellId++);
				cell.setCellValue(CommonConstants.LAST_24_HRS_DATA); 
				cell = mainHeaderRow.createCell(cellId++);
				cell.setCellValue(CommonConstants.LAST_7_DAYS_DATA);
				cell = mainHeaderRow.createCell(cellId++);
				cell.setCellValue(CommonConstants.USER_ID); 
				cell = mainHeaderRow.createCell(cellId++);
				cell.setCellValue(CommonConstants.USER_MOBILE_NO);
				cell = mainHeaderRow.createCell(cellId++);
				cell.setCellValue(CommonConstants.USER_DETAILS); 
				//TODO: Write a separate method write to excel

				for(Map.Entry<UUID, Map<String, String>> projectDataValue : projectValues.entrySet()) {
					UUID prUUID = projectDataValue.getKey();
					XSSFRow dataRow = worksheet.createRow(rowId++);
					cellId = 0;
					Map<String, String> projectDataValueItem = projectDataValue.getValue();	
					for(Map.Entry<String, Set<String>> projectFieldMapEntry : projectToFieldMap.entrySet()) {
						boolean isValueSet = false;
						cell = dataRow.createCell(cellId++);
						for(Map.Entry<String, String> projDataSet : projectDataValueItem.entrySet()) {
							if(projectFieldMapEntry.getValue().contains(projDataSet.getKey())){
								cell.setCellValue(projDataSet.getValue());
								isValueSet = true;
							}
						}
						if(isValueSet == false) {
							cell.setCellValue(CommonConstants.NA);
						}
					}
					cell = dataRow.createCell(cellId++);
					if(submissionCountForLast24Hrs.get(prUUID) != null) {
						cell.setCellValue(submissionCountForLast24Hrs.get(prUUID));
					}else {
						cell.setCellValue(CommonConstants.NA);
					}
					cell = dataRow.createCell(cellId++);
					if(submissionCountForLast7Days.get(prUUID) != null) {
						cell.setCellValue(submissionCountForLast7Days.get(prUUID));
					}else {
						cell.setCellValue(CommonConstants.NA);
					}
					
					Map<String, UserDBMetaData> userDataMap = projectUserMappingData.get(prUUID);
					if(userDataMap == null) {
						logger.info("User data value not present for project "+ prUUID);
						continue;
					}
					for(Map.Entry<String, UserDBMetaData> userMetaData : userDataMap.entrySet()) {
						cell = dataRow.createCell(cellId++);
						cell.setCellValue(userMetaData.getKey());
						if(userMetaData.getValue() != null) {
							cell = dataRow.createCell(cellId++);
							cell.setCellValue(userMetaData.getValue().getMobileNumber());
							cell = dataRow.createCell(cellId++);
							cell.setCellValue(userMetaData.getValue().getUserDetails());
						}
						else {
							cell = dataRow.createCell(cellId++);
							cell.setCellValue(CommonConstants.NA);
							cell = dataRow.createCell(cellId++);
							cell.setCellValue(CommonConstants.NA);
						}
					}
				}
			}

			workbook.write(file);
			file.close();
			workbook.close();
		}

		String mailReceivers = properties.getProperty("mail_receivers");
		List<String> toList = Arrays.asList(mailReceivers.split("##"));
		emailUsageExcelData(toList, CommonConstants.MAIL_SUBJECT_FOR_USAGE_DATA, CommonConstants.MAIL_BODY_FOR_USAGE_DATA, true, superAppFilePaths);
		
	}

	private Map<UUID, Map<String, UserDBMetaData>> getProjectUserMappingData(List<UserProjectMapping> userProjectMapping, List<String> userIdList,
			UUID superApplicationId, UUID applicationId, UserStates userState) {
		 
		 Map<UUID, Map<String, UserDBMetaData>> projectToUserDataMap = new HashMap<>();
		 Map<String, UserDBMetaData> userMetaDataMap = userMetaDataCrudService.getMetaDataForListOfUsers(superApplicationId, userIdList, userState);
		 if(userMetaDataMap == null) {
			 return projectToUserDataMap;
		 }
		 for(UserProjectMapping userProjectData : userProjectMapping) {
			 if(userProjectData == null) {
				 logger.info("User project data is not present");
				 continue;
			 }
			 String userId = userProjectData.getUserId(); 
			 //Skipping the default user id
			 if(userId.equalsIgnoreCase(CommonConstants.DEFAULT_USER_ID))
				 continue;
			 List<UUID> projectList = userProjectData.getProjectList();
			 for(UUID projectUUID : projectList) {
				 Map<String, UserDBMetaData> userMetaDataMapValue;
				 if(projectToUserDataMap.get(projectUUID) == null) {
					 userMetaDataMapValue = new HashMap<>();
				 }
				 else {
					 userMetaDataMapValue = projectToUserDataMap.get(projectUUID);
				 }
				 userMetaDataMapValue.put(userId, userMetaDataMap.get(userId));
				 projectToUserDataMap.put(projectUUID, userMetaDataMapValue);
			 }
		 }
		 return projectToUserDataMap;
	}

	private void updateSubmissionCountForProjects(Map<UUID, Integer> submissionCount,
			List<UserTrackingData> userTrackingDataList) {
		if (userTrackingDataList == null || userTrackingDataList.isEmpty()) {
			logger.info("User tracking data list is not present");
			return;
		}
		for (UserTrackingData userTrackingData : userTrackingDataList) {
			if (userTrackingData == null) {
				logger.info("User Tracking data is null");
			}
			String requestObj = userTrackingData.getRequestObj();
			if (requestObj != null && !requestObj.equals("")) {
				try {
					AppFormData formInsertedData = objectMapper.readValue(requestObj, AppFormData.class);
					UUID projectId = formInsertedData.getProjectId();
					if (submissionCount.get(projectId) == null) {
						submissionCount.put(projectId, 1);
					} else {
						submissionCount.put(projectId, submissionCount.get(projectId) + 1);
					}
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param toList --> list of persons to receive email
	 * @param subject
	 * @param body
	 * @param hasAttachment --> true/false
	 * @param attachmentList --> if has attachment is true send a list of attachments
	 */
	private void emailUsageExcelData(List<String> toList, String subject, String body, boolean hasAttachment, List<String> attachmentList) {
		Message ms = new Message();
		ms.setSubject(subject);
		ms.setToList(toList);
		ms.setMessage(body);
		if(hasAttachment) {
			ms.setAttachments(attachmentList);
		}
		try {
			emailMessagingServiceImpl.sendMessage(ms, hasAttachment);
		} catch (MailException e) {
			e.printStackTrace();
			System.out.println("exception mail");
		} catch (InvalidInputParamException e) {
			e.printStackTrace();
			System.out.println("invalid param exception");
		}
	}
	/*
	 * private void updateSubmissionCountForProjects(Map<UUID, CountAndUserData>
	 * submissionCount, List<UserTrackingData> userTrackingDataList) {
	 * if(userTrackingDataList == null || userTrackingDataList.isEmpty()) {
	 * logger.info("User tracking data list is not present"); return; }
	 * for(UserTrackingData userTrackingData: userTrackingDataList) {
	 * if(userTrackingData == null) { logger.info("User Tracking data is null"); }
	 * String requestObj = userTrackingData.getRequestObj(); if(requestObj != null
	 * && !requestObj.equals("")) { try { FormInsertedData formInsertedData =
	 * objectMapper.readValue(requestObj, FormInsertedData.class); UUID projectId =
	 * formInsertedData.getProjectId(); if(submissionCount.get(projectId) == null) {
	 * CountAndUserData countAndUserData = new CountAndUserData();
	 * countAndUserData.setCount(1); Set<String> userIdList = new HashSet<>();
	 * userIdList.add(userTrackingData.getUserId());
	 * countAndUserData.setUserIdList(userIdList); submissionCount.put(projectId,
	 * countAndUserData); } else { CountAndUserData countAndUserData =
	 * submissionCount.get(projectId); if(countAndUserData != null) {
	 * countAndUserData.setCount(countAndUserData.getCount() + 1); Set<String>
	 * userIdList = countAndUserData.getUserIdList();
	 * userIdList.add(userTrackingData.getUserId());
	 * countAndUserData.setUserIdList(userIdList); submissionCount.put(projectId,
	 * countAndUserData); } else { logger.info("Count and user data is null"); } } }
	 * catch (JsonParseException e) { e.printStackTrace(); } catch
	 * (JsonMappingException e) { e.printStackTrace(); } catch (IOException e) {
	 * e.printStackTrace(); } } } }
	 */
}
