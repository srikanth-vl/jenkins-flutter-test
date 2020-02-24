package com.vassarlabs.proj.uniapp.data.retrieve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.DataSubmitField;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaDataAttributes;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.constants.ProjectListConstants;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FormSubmittedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;

@Component
public class DataRetrievalService {
	
	@Autowired ProjectMasterDataCrudService projectMasterDataCrudService;
	@Autowired FormSubmittedDataCrudService submittedDataCrudService;
	@Autowired FieldMetaDataCrudService fieldMetaDataCrudService;
	
	private String getMasterValue(String key, ProjectMasterData projectMasterData, FormSubmitData formData) 
			throws DataNotFoundException {
		
		if(projectMasterData == null) {
			throw new DataNotFoundException();
		} else {
			if(formData != null) {
				if(formData.getDate() <= projectMasterData.getDate()
						&& projectMasterData.getInsertTs() > formData.getTimestamp()) {
					return projectMasterData.getValue();
				} else {
					return formData.getValue();
				}
			} else {
				return projectMasterData.getValue();
			}
		}
	}
	
	private DataSubmitField getMasterValueWithTimestamp(String key, ProjectMasterData projectMasterData, FormSubmitData formData) throws DataNotFoundException {
		
		if(projectMasterData == null) {
			throw new DataNotFoundException();
		} else {
			if(formData != null) {
				if(formData.getDate() <= projectMasterData.getDate()
						&& projectMasterData.getInsertTs() > formData.getTimestamp()) {
					return new DataSubmitField(projectMasterData.getValue(), null);
				} else {
					return new DataSubmitField(formData.getValue(), getTimestamp(formData));
				}
			}
			else {
				return new DataSubmitField(projectMasterData.getValue(), null);
			}
		}
	}
	
	private Long getTimestamp(FormSubmitData formData) {
		if(formData.getDate() == DateUtils.getYYYYMMdd(formData.getTimestamp()))
			return formData.getTimestamp();
		else
			return DateUtils.getTimestamp(String.valueOf(formData.getDate()), ProjectListConstants.DB_DATE_FORMAT);
	}
	
	private Map<String, FieldMetaData> getFieldMetaData(List<FieldMetaData> list){
		Map<String, FieldMetaData> fieldMetaData = new HashMap<>();
//		fieldMetaData.putAll(list.stream().filter(e -> e.getKeyType() == KeyTypes.MASTER_DATA_KEY.getValue()).collect(Collectors.toMap(fieldData -> fieldData.getKey(), fieldData -> fieldData)));
//		fieldMetaData.putAll(list.stream().filter(e -> e.getKeyType() == KeyTypes.APP_DATA_KEY.getValue()).collect(Collectors.toMap(fieldData -> fieldData.getKey(), fieldData -> fieldData)));
		fieldMetaData.putAll(list.stream().filter(e -> e.getKeyType() == KeyTypes.MASTER_DATA_KEY.getValue()).collect(Collectors.toMap(fieldData -> fieldData.getKey(), fieldData -> fieldData, (oldValue, newValue) -> oldValue)));
		fieldMetaData.putAll(list.stream().filter(e -> e.getKeyType() == KeyTypes.APP_DATA_KEY.getValue()).collect(Collectors.toMap(fieldData -> fieldData.getKey(), fieldData -> fieldData,  (oldValue, newValue) -> oldValue)));
		
		return fieldMetaData;
	}
	
	public Map<String, String> getValueForAProjectWithSyncTSAndUserId(UUID superAppId, UUID appId, UUID projectId, ProjectStates state) throws DataNotFoundException, JsonParseException, JsonMappingException, IOException {
		
		Map<String, String> projectDataMap;
		Map<String, ProjectMasterData> keyToMasterDataMap;
		List<FormSubmitData> latestFormDataList; 
		Map<String, FormSubmitData> keyToAppDataMap;
		List<ProjectMasterData> projectMasterDefaultDateList;
		List<String> metaDataIds = new ArrayList<String>();
		Map<UUID, List<FieldMetaData>> projectIdToFieldMetaDataMap = null;
		String lastSyncTimestamp;
		
		keyToMasterDataMap = projectMasterDataCrudService.getKeyToTargetValue(superAppId, appId, projectId, state);
		latestFormDataList = submittedDataCrudService.findLatestValuesOfKeys(superAppId, appId, projectId);
		projectMasterDefaultDateList = projectMasterDataCrudService.findDataForProjectIdsAndDefaultDate(superAppId, appId, Arrays.asList(projectId), state);
		
		if(latestFormDataList != null && latestFormDataList.size() > 0)
		{
			metaDataIds.addAll(latestFormDataList.stream().map(e -> e.getMetaDataInstanceId()).collect(Collectors.toSet()));
		}
		
		projectIdToFieldMetaDataMap = fieldMetaDataCrudService.getProjectIdAndDefaultIdToFieldsData(superAppId, appId, metaDataIds);
		
		keyToAppDataMap = latestFormDataList.stream().collect(Collectors.toMap(FormSubmitData::getKey, Function.identity()));
		Optional<ProjectMasterData> optionalResult = projectMasterDefaultDateList.stream().filter(x -> x.getKey().equals(MasterDataKeyNames.LAST_SYNC_TS)).max(Comparator.comparing(ProjectMasterData::getInsertTs));
		lastSyncTimestamp = optionalResult.isPresent() ? optionalResult.get().getValue() : null;
		
	    projectDataMap = getExternalKeyValueForAProject(keyToMasterDataMap, keyToAppDataMap, projectIdToFieldMetaDataMap == null ? null : getFieldMetaData(Optional.ofNullable(projectIdToFieldMetaDataMap.get(projectId)).orElse(projectIdToFieldMetaDataMap.get(UUIDUtils.getDefaultUUID()))));
	    
	    projectDataMap.put(MasterDataKeyNames.LAST_SYNC_TS, (lastSyncTimestamp == null || lastSyncTimestamp.isEmpty()) ? null : lastSyncTimestamp);
	    projectDataMap.put(CommonConstants.LAST_SYNC_USER_ID, (keyToAppDataMap == null || keyToAppDataMap.size() == 0) ? null : latestFormDataList.stream().max(Comparator.comparing(FormSubmitData::getTimestamp)).get().getUserId());
	    
	    return projectDataMap;
	}
	
	public Map<UUID, Map<String, String>> getValueForListOfProjectWithSyncTSAndUserId(UUID superAppId, UUID appId, List<UUID> projectIdList) throws DataNotFoundException, JsonParseException, JsonMappingException, IOException {
		
		Map<UUID, Map<String, String>> projectDataMap = new HashMap<>();
		Map<UUID, Map<String, ProjectMasterData>> keyToMasterDataMap;
		Map<UUID, Map<String, FormSubmitData>> latestFormDataMap;
		Map<String, String> latestdataMap;
		Map<String, ProjectMasterData> masterDataMap;
		Map<String, FormSubmitData> appDataMap;
		Map<UUID, List<FieldMetaData>> projectIdToFieldMetaDataMap = null;
		List<String> metaDataIds = new ArrayList<String>();
		Map<UUID, Set<String>> metaDataInstanceIdsMap = new HashMap<>();
		
		keyToMasterDataMap = projectMasterDataCrudService.getAllMasterDataForProjectIds(superAppId, appId, projectIdList);
		latestFormDataMap = submittedDataCrudService.findLatestFormSubmittedDataOfKeys(superAppId, appId, projectIdList);
		
		if(latestFormDataMap != null && latestFormDataMap.size() > 0)
		{
			metaDataInstanceIdsMap = latestFormDataMap.entrySet().stream().collect(Collectors.toMap(appData -> appData.getKey(), e -> e.getValue().values().stream().map(formSubmitData -> formSubmitData.getMetaDataInstanceId()).collect(Collectors.toSet())));
			for(Entry<UUID, Set<String>> metaData : metaDataInstanceIdsMap.entrySet())
				metaDataIds.addAll(metaData.getValue());
		}
		
		projectIdToFieldMetaDataMap = fieldMetaDataCrudService.getProjectIdAndDefaultIdToFieldsData(superAppId, appId, metaDataIds);
		
		for(UUID projectId : projectIdList) {
			masterDataMap = keyToMasterDataMap.get(projectId);
			appDataMap = latestFormDataMap.get(projectId);
			latestdataMap = getExternalKeyValueForAProject(masterDataMap, appDataMap, projectIdToFieldMetaDataMap == null ? null : getFieldMetaData(Optional.ofNullable(projectIdToFieldMetaDataMap.get(projectId)).orElse(projectIdToFieldMetaDataMap.get(UUIDUtils.getDefaultUUID()))));
			latestdataMap.put(CommonConstants.LAST_SYNC_USER_ID, (appDataMap == null || appDataMap.size() == 0) ? null : appDataMap.values().stream().max(Comparator.comparing(FormSubmitData::getTimestamp)).get().getUserId());
		    	projectDataMap.put(projectId, latestdataMap);
		}
		
	    return projectDataMap;
	}
	
	
	public Map<UUID, Map<String, DataSubmitField>> getValueForListOfProjectWithSyncTSUserIdAndDataSubmitField(UUID superAppId, UUID appId, List<UUID> projectIdList) throws DataNotFoundException, JsonParseException, JsonMappingException, IOException {
		
		Map<UUID, Map<String, DataSubmitField>> projectDataMap = new HashMap<>();
		Map<UUID, Map<String, ProjectMasterData>> keyToMasterDataMap;
		Map<UUID, Map<String, FormSubmitData>> latestFormDataMap;
		Map<String, DataSubmitField> latestdataMap;
		Map<String, ProjectMasterData> masterDataMap;
		Map<String, FormSubmitData> appDataMap;
		Map<UUID, List<FieldMetaData>> projectIdToFieldMetaDataMap = null;
		Map<UUID, Set<String>> metaDataInstanceIdsMap = new HashMap<>();
		List<String> metaDataIds = new ArrayList<String>();
		FormSubmitData lastSyncUserId;
		
		keyToMasterDataMap = projectMasterDataCrudService.getAllMasterDataForProjectIds(superAppId, appId, projectIdList);
		latestFormDataMap = submittedDataCrudService.findLatestFormSubmittedDataOfKeys(superAppId, appId, projectIdList);
		
		if(latestFormDataMap != null && latestFormDataMap.size() > 0)
		{
			metaDataInstanceIdsMap = latestFormDataMap.entrySet().stream().collect(Collectors.toMap(appData -> appData.getKey(), e -> e.getValue().values().stream().map(formSubmitData -> formSubmitData.getMetaDataInstanceId()).collect(Collectors.toSet())));
			for(Entry<UUID, Set<String>> metaData : metaDataInstanceIdsMap.entrySet())
				metaDataIds.addAll(metaData.getValue());
		}
		
		projectIdToFieldMetaDataMap = fieldMetaDataCrudService.getProjectIdAndDefaultIdToFieldsData(superAppId, appId, metaDataIds);
		
		for(UUID projectId : projectIdList) {
			masterDataMap = keyToMasterDataMap.get(projectId);
			appDataMap = latestFormDataMap.get(projectId);
			latestdataMap = getExternalKeyDataSubmitFieldForAProject(masterDataMap, appDataMap, projectIdToFieldMetaDataMap == null ? null : getFieldMetaData(Optional.ofNullable(projectIdToFieldMetaDataMap.get(projectId)).orElse(projectIdToFieldMetaDataMap.get(UUIDUtils.getDefaultUUID()))));
			lastSyncUserId = (appDataMap == null || appDataMap.size() == 0) ? null : appDataMap.values().stream().max(Comparator.comparing(FormSubmitData::getTimestamp)).get();
			latestdataMap.put(CommonConstants.LAST_SYNC_USER_ID, lastSyncUserId == null ? null : new DataSubmitField(lastSyncUserId.getValue(), lastSyncUserId.getTimestamp()));
			projectDataMap.put(projectId, latestdataMap);
		}
		
	    return projectDataMap;
	}

	private Map<String, String> getExternalKeyValueForAProject(Map<String, ProjectMasterData> keyToMasterDataMap, Map<String, FormSubmitData> keyToAppDataMap, Map<String, FieldMetaData> metaDataMap) throws DataNotFoundException, JsonParseException, JsonMappingException, IOException {
		
		Map<String, String> keyToValuesMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		String externalKey;
		
		if(keyToMasterDataMap != null) {
		    for(String key : keyToMasterDataMap.keySet()) {
		    	if(metaDataMap == null || metaDataMap.get(key) == null || metaDataMap.get(key).getAttributes() == null || objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey() == null || objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey().isEmpty())
		    		externalKey = key;
		    	else
		    		externalKey = objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey();
		    	keyToValuesMap.put(externalKey, getMasterValue(key, keyToMasterDataMap.get(key), keyToAppDataMap == null ? null : keyToAppDataMap.get(key)));
		    }
		}
		
		if(keyToAppDataMap != null) {
		    for(String key : keyToAppDataMap.keySet()) {
		    	if(metaDataMap == null || metaDataMap.get(key) == null || metaDataMap.get(key).getAttributes() == null || objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey() == null || objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey().isEmpty())
		    		externalKey = key;
		    	else
		    		externalKey = objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey();
		    	if(keyToValuesMap.get(externalKey) == null)
		    		keyToValuesMap.put(externalKey, keyToAppDataMap.get(key).getValue());
		    }
		}
		
	    return keyToValuesMap;
	}

	private Map<String, DataSubmitField> getExternalKeyDataSubmitFieldForAProject(Map<String, ProjectMasterData> keyToMasterDataMap, Map<String, FormSubmitData> keyToAppDataMap, Map<String, FieldMetaData> metaDataMap) throws DataNotFoundException, JsonParseException, JsonMappingException, IOException {
		
		Map<String, DataSubmitField> keyToValuesMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		String externalKey;
		
		if(keyToMasterDataMap != null) {
		    for(String key : keyToMasterDataMap.keySet()) {
		    	if(metaDataMap == null || metaDataMap.get(key) == null || metaDataMap.get(key).getAttributes() == null || objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey() == null || objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey().isEmpty())
		    		externalKey = key;
		    	else
		    		externalKey = objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey();
		    	keyToValuesMap.put(externalKey, getMasterValueWithTimestamp(key, keyToMasterDataMap.get(key), keyToAppDataMap == null ? null : keyToAppDataMap.get(key)));
		    }
		}
		
		if(keyToAppDataMap != null) {
		    for(String key : keyToAppDataMap.keySet()) {
		    	if(metaDataMap == null || metaDataMap.get(key) == null || metaDataMap.get(key).getAttributes() == null || objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey() == null || objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey().isEmpty())
		    		externalKey = key;
		    	else
		    		externalKey = objMapper.readValue(metaDataMap.get(key).getAttributes(), FieldMetaDataAttributes.class).getExternalKey();
		    	if(keyToValuesMap.get(externalKey) == null)
		    		keyToValuesMap.put(externalKey, new DataSubmitField(keyToAppDataMap.get(key).getValue(), getTimestamp(keyToAppDataMap.get(key))));
		    }
		}
		
	    return keyToValuesMap;
	}

	public Map<String, String> getValueForAProject(UUID superAppId, UUID appId, UUID projectId, List<ProjectMasterData> masterDataList) throws DataNotFoundException {
		
		Map<String, ProjectMasterData> keyToMasterDataMap = masterDataList.stream().
				collect(Collectors.toMap(ProjectMasterData::getKey, Function.identity()));
		
		List<FormSubmitData> latestFormDataList = submittedDataCrudService.findLatestValuesOfKeys(superAppId, appId, projectId);
	    Map<String, FormSubmitData> keyToAppDataMap = latestFormDataList.stream().
	    		collect(Collectors.toMap(FormSubmitData::getKey, Function.identity()));
	    
	    return getValueForAProject(keyToMasterDataMap, keyToAppDataMap);
	}
	
	public Map<String, String> getValueForAProject(UUID superAppId, UUID appId, UUID projectId, ProjectStates state) throws DataNotFoundException {
		
		Map<String, ProjectMasterData> keyToMasterDataMap = projectMasterDataCrudService.getKeyToTargetValue(superAppId, appId, projectId, state);
		
		List<FormSubmitData> latestFormDataList = submittedDataCrudService.findLatestValuesOfKeys(superAppId, appId, projectId);
	    Map<String, FormSubmitData> keyToAppDataMap = latestFormDataList.stream().
	    		collect(Collectors.toMap(FormSubmitData::getKey, Function.identity()));
	    return getValueForAProject(keyToMasterDataMap, keyToAppDataMap);
	}
	
	public Map<UUID, Map<String, String>> getValueForAProject(UUID superAppId, UUID appId, List<UUID> projectIdList, ProjectStates state) throws DataNotFoundException {
		
		Map<UUID, Map<String, String>> projectIdToDataMap = new HashMap<>();
		for(UUID projectId : projectIdList) {
			projectIdToDataMap.put(projectId, getValueForAProject(superAppId, appId, projectId, state));
		}
		return projectIdToDataMap;
	}
	
	private Map<String, String> getValueForAProject(Map<String, ProjectMasterData> keyToMasterDataMap, Map<String, FormSubmitData> keyToAppDataMap) throws DataNotFoundException {
		  
		Map<String, String> keyToValuesMap = new HashMap<>();
		
		if(keyToMasterDataMap != null) {
		    for(String key : keyToMasterDataMap.keySet()) {
		    	keyToValuesMap.put(key, getMasterValue(key, keyToMasterDataMap.get(key), keyToAppDataMap == null ? null : keyToAppDataMap.get(key)));
		    }
		}
		
		if(keyToAppDataMap != null) {
		    for(String key : keyToAppDataMap.keySet()) {
		    	if(keyToValuesMap.get(key) == null)
		    		keyToValuesMap.put(key, keyToAppDataMap.get(key).getValue());
		    }
		}
		
	    return keyToValuesMap;
	}
	
	public Map<UUID, List<ProjectMasterData>> getProjectIdToListOfMasterData(UUID superAppId, UUID appId, List<UUID> projectIdList, List<ProjectStates> states) throws DataNotFoundException {
		
		Map<UUID, Integer> projectIdToLatestDateMap = projectMasterDataCrudService.findProjectToLatestDateMap(superAppId, appId, projectIdList, states);
		List<ProjectMasterData> projectMasterDataList = projectMasterDataCrudService.findDataForProjectIdAndDate(superAppId, appId, projectIdToLatestDateMap, states);
		Map<UUID, List<ProjectMasterData>> projectIdToMasterDataListMap = new HashMap<>();
		for(ProjectMasterData masterData : projectMasterDataList) {
			UUID projectId = masterData.getProjectId();
			if(!projectIdToMasterDataListMap.containsKey(masterData.getProjectId())) {
				projectIdToMasterDataListMap.put(projectId, new ArrayList<>());
			}
			List<ProjectMasterData> masterDataList = projectIdToMasterDataListMap.get(projectId);
			masterDataList.add(masterData);
			projectIdToMasterDataListMap.put(projectId, masterDataList);
		}
		
		return projectIdToMasterDataListMap;
	}
	
	public Map<String, FormSubmitData> getValueObjectForAProject(UUID superAppId, UUID appId, UUID projectId, List<ProjectMasterData> masterDataList) throws DataNotFoundException {

		Map<String, ProjectMasterData> keyToMasterDataMap = masterDataList.stream().
				collect(Collectors.toMap(ProjectMasterData::getKey, Function.identity()));

		List<FormSubmitData> latestFormDataList = submittedDataCrudService.findLatestValuesOfKeys(superAppId, appId, projectId);
		Map<String, FormSubmitData> keyToAppDataMap = latestFormDataList.stream().
				collect(Collectors.toMap(FormSubmitData::getKey, Function.identity()));

		Map<String, FormSubmitData> keyToValuesMap = new HashMap<>();

		if(keyToMasterDataMap != null) {
			for(String key : keyToMasterDataMap.keySet()) {
				ProjectMasterData projectMasterData = keyToMasterDataMap.get(key);
				FormSubmitData formData = keyToAppDataMap == null ? null : keyToAppDataMap.get(key);
				if(projectMasterData == null) {
					throw new DataNotFoundException();
				} else {
					if(formData != null) {
						if(formData.getDate() <= projectMasterData.getDate()
								&& projectMasterData.getInsertTs() > formData.getTimestamp()) {
							formData.setValue(projectMasterData.getValue());
							formData.setTimestamp(CommonConstants.DEFAULT_LONG_VALUE);
							formData.setDate(CommonConstants.DEFAULT_INT_VALUE);
							formData.setUserId(CommonConstants.DEFAULT_USER_ID);
							formData.setUserType(UserPriorities.Default.getValue());
							keyToValuesMap.put(key, formData);
						} else {
							keyToValuesMap.put(key, formData);
						}
					} else {
						formData = new FormSubmitData();
						formData.setSuperAppId(projectMasterData.getSuperAppId());
						formData.setApplicationId(projectMasterData.getApplicationId());
						formData.setProjectId(projectMasterData.getProjectId());
						formData.setKey(projectMasterData.getKey());
						formData.setValue(projectMasterData.getValue());
						formData.setTimestamp(CommonConstants.DEFAULT_LONG_VALUE);
						formData.setDate(CommonConstants.DEFAULT_INT_VALUE);
						formData.setUserId(CommonConstants.DEFAULT_USER_ID);
						formData.setUserType(UserPriorities.Default.getValue());
						keyToValuesMap.put(key, formData);
					}
				}
			}
		}

		if(keyToAppDataMap != null) {
			for(String key : keyToAppDataMap.keySet()) {
				if(keyToValuesMap.get(key) == null)
					keyToValuesMap.put(key, keyToAppDataMap.get(key));
			}
		}

		return keyToValuesMap;
	}
	/**
	 * For each project -> if submission timestamp from app  > max(upload ts) of keys --> return submission time, 
	 * else return upload_ts
	 * @param superAppId
	 * @param appId
	 * @param projectIdList
	 * @return
	 */
	public Map<UUID, Long> getLastUpdatedTSforProjectList(UUID superAppId, UUID appId, List<UUID> projectIdList) {
		Map<UUID, Long> projectIdToLastSyncTS = new HashMap<>();
		Map<UUID, Map<String, ProjectMasterData>> keyToMasterDataMap = projectMasterDataCrudService.getAllMasterDataForProjectIds(superAppId, appId, projectIdList);
		for(UUID projectId : keyToMasterDataMap.keySet()) {
			Map<String, ProjectMasterData> keyToMasterData = keyToMasterDataMap.get(projectId);
			// Add projects whose status is not deleted
			if(keyToMasterData.containsKey(MasterDataKeyNames.STATE_KEY)
					&& !keyToMasterData.get(MasterDataKeyNames.STATE_KEY).getValue().equalsIgnoreCase(ProjectStates.DELETED.getValue())) {
				long lastSyncTS = -1;
				for(String key : keyToMasterData.keySet()) {
					// If upload TS > last sync TS --> latest TS = upload TS
					if(lastSyncTS < keyToMasterData.get(key).getInsertTs()) {
						lastSyncTS = keyToMasterData.get(key).getInsertTs();
						projectIdToLastSyncTS.put(projectId, lastSyncTS);
					}
				}
			}
		}
		return projectIdToLastSyncTS;
	}
}
