package com.vassarlabs.proj.uniapp.entitymetadataconfig.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.workdocs.model.UserMetadata;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.EntityConfigInfo;
import com.vassarlabs.proj.uniapp.api.pojo.EntityElement;
import com.vassarlabs.proj.uniapp.api.pojo.EntityMetadataConfigRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.IEntityMetadataConfigurationService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.EntityMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.EntityMetadataConfigurationConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.EntityMetadataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class EntityMetadataConfigurationServiceUserSpecific	 {
	@Autowired
	private EntityMetadataCrudService entityMetadataCrudService;
	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	@Autowired
	private UserMetaDataCrudService userMetaDataCrudService;
	@Autowired
	private SuperAppDataCrudService superAppDataCrudService;
	public ServiceOutputObject getEntityConfigData(EntityMetadataConfigRequestObject requestObject) 
			throws IOException, TokenNotFoundException, TokenExpiredException{
		if(requestObject == null) {
			logger.error("EntityMetadataConfigRequestObject is NULL");
			return null;
		}
		
		List<EntityMetaData> entityConfigList =  new ArrayList<>(); 
		UUID superAppId = requestObject.getSuperAppId();
		UUID appId = requestObject.getAppId(); 
		if(appId == null ) {
			appId = UUIDUtils.getDefaultUUID();
		}
		UUID projectId = requestObject.getProjectId();
		UUID token = requestObject.getTokenId();
		String userId = requestObject.getUserId();
		String parentEntity = requestObject.getParentEntity();
		String entityName= requestObject.getEntityName();
		Long appMaxInsertTs = requestObject.getLastSyncTs();
		UserDBMetaData userDBMetaData  = userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userId, UserStates.ACTIVE);
		entityConfigList = entityMetadataCrudService.getLatestEntityMetadataForApp(superAppId, appId);
		
		List<EntityMetaData> deltaEntityConfigList =  new ArrayList<>(); 
//		for (EntityMetaData entityMetaData : entityConfigList) {
//			if(entityMetaData.getInsertTs() > appMaxInsertTs) {
//				deltaEntityConfigList.add(entityMetaData);
//			}
//		}
		Map<String, String> userAdditionalProperties=  new HashMap<>();
		
		if(userDBMetaData.getUserDetails() != null && !userDBMetaData.getUserDetails().isEmpty()) {
			userAdditionalProperties = objectMapper.readValue(userDBMetaData.getUserDetails(), new TypeReference<Map<String,String>>() {});
		}
		Map<String, Map<String, List<EntityMetaData>>> configMap = new  HashMap<>();
		getExistingEntity(configMap,entityConfigList);
		String entityConfigStr = superAppDataCrudService.getEntityConfig(superAppId);
		List<EntityConfigInfo> configInfo =  new  ArrayList<>();
		if(entityConfigStr !=  null) {
			configInfo = objectMapper.readValue(entityConfigStr, new TypeReference<List<EntityConfigInfo>>() {});
			for (EntityConfigInfo entityConfigInfo : configInfo) {
				filterEntites(entityConfigInfo,deltaEntityConfigList,configMap, userAdditionalProperties) ;
			}
		}
		long trackingTS =  System.currentTimeMillis();
		UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId,  requestObject.getUserId(), token, APITypes.ENTITY_META_DATA_CONFIG,
				ServiceNamesConstants.ENTITY_META_DATA_CONFIG_NAME, objectMapper.writeValueAsString(requestObject), null, true, trackingTS);
		Map<String, Object> entityConfig = new HashMap<>();
		entityConfig.put(EntityMetadataConfigurationConstants.ENTITY_CONFIG, deltaEntityConfigList);
		entityConfig.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
		ServiceOutputObject output = new ServiceOutputObject(entityConfig, trackingObject, true);
		return output;
	}
	public void filterEntites(EntityConfigInfo entityConfig, List<EntityMetaData> data,Map<String, Map<String, 
			List<EntityMetaData>>> configMap,Map<String, String> userAdditionalProperties ) {
		String type= entityConfig.getName();
		String parentExpression = entityConfig.getParent();
		
		String filter = entityConfig.getFilter();
		List<EntityConfigInfo> childEntity =  entityConfig.getChildren();

		if(filter.equals("ALL")) {
			if(parentExpression == null || parentExpression.equals("DEFAULT")) {
				for (String parentEntity : configMap.keySet()) {
					if(parentEntity.equals("DEFAULT")  || parentEntity.contains("##" + type+"$$"));
					{
						Map<String, List<EntityMetaData>> typeToDataMap = configMap.get(parentEntity);
						for (String typeEntity: typeToDataMap.keySet() ) {
							if((parentEntity.equals("DEFAULT") && typeEntity.equals(type)) ||(parentEntity.contains("##" + type+"$$"))) {
								data.addAll(typeToDataMap.get(typeEntity));
							} 
						}
					}
				}
			} else {
				List<String> parent = getParent(userAdditionalProperties, parentExpression, type);
				List<String> childrenFilterExpression =  new ArrayList<>();
				for (String parentValue : parent) {
					childrenFilterExpression.add(parentValue+"##" + type+"$$");
				}
				for (String parentEntity : configMap.keySet()) {
					if(parent.contains(parentEntity)  || ifChild(childrenFilterExpression,parentEntity));
					{
						Map<String, List<EntityMetaData>> typeToDataMap = configMap.get(parentEntity);
						for (String typeEntity: typeToDataMap.keySet() ) {
							
							if((parent.contains(parentEntity) && typeEntity.equals(type)) ||(ifChild(childrenFilterExpression,parentEntity))) {
								data.addAll(typeToDataMap.get(typeEntity));
							} 
						}
					}
				}
			}
		} 
			else if(filter.equals("NONE")) {
			for (EntityConfigInfo entityConfigInfo : childEntity) {
				filterEntites(entityConfigInfo,data,configMap, userAdditionalProperties) ;
			}
		}
	}
	public boolean ifChild(List<String> childrenFilterExpression, String parentEntity) {
		boolean flag =  false;
		for (String value : childrenFilterExpression) {
			if(parentEntity.contains(value))
			{
				flag = true;
			}
		}
		return flag;
		
	}
	public List<String> getParent(Map<String, String> keyToValuesListString, String parentExpression, String entityType) {

		List<String> parent = new ArrayList<>();
		if(parentExpression== null || parentExpression.equalsIgnoreCase("DEFAULT")) {
			parent.add("DEFAULT");
			return parent;
		}
		if(keyToValuesListString == null || keyToValuesListString.isEmpty()) {
			return parent;
		}
		Map<String, List<String>> keyToVlauesList = new HashMap<>();
		String parentValue="DEFAULT";
		List<String> parentDimensions   =  StringUtils.getStringListFromDelimitter("##", parentExpression);
		for (String dimension : parentDimensions) {
			if(keyToValuesListString.get(dimension)!= null && !keyToValuesListString.get(dimension).isEmpty()){
				String values = keyToValuesListString.get(dimension);
				values = values.substring(1, values.length()-1);
				List<String> list =  new ArrayList<>();
				list = StringUtils.getStringListFromDelimitter(",", values);
				keyToVlauesList.put(dimension,list );
				if(list.size()== 0)
				{return parent;}

				if(parentDimensions.get(parentDimensions.size()-1).equals(dimension)) {
					for (String lastDimensionValue : list) {
						String finalParent = parentValue + "##" + dimension + "$$" + lastDimensionValue.trim();
						parent.add(finalParent);
					}

				} else {
					parentValue = parentValue + "##" + dimension + "$$" + keyToVlauesList.get(dimension).get(0).trim();
				}

			}
		}
		return parent;
	}
	
	public void getExistingEntity(Map<String, Map<String, List<EntityMetaData>>> configMap, List<EntityMetaData> data) {
		for (EntityMetaData entityData : data) {
			String type = entityData.getEntityName();
			String parent = entityData.getParentEntity();
			updateEntityConfigMap(configMap,entityData,parent, type);
		}
	}
	public void updateEntityConfigMap(Map<String, Map<String,List<EntityMetaData>>> entityConfigMap, EntityMetaData data, String parent, String type) {
		Map<String,List<EntityMetaData>> typeToElementsMap = entityConfigMap.get(parent);
		List<EntityMetaData> elements = new ArrayList<>();
		if(typeToElementsMap == null) {
			typeToElementsMap =  new HashMap<>();
		}
		elements = typeToElementsMap.get(type);
		if(elements == null) {
			elements = new ArrayList<>();
		} 
		elements.add(data);
		typeToElementsMap.put(type, elements);
		entityConfigMap.put(parent, typeToElementsMap);
	}

}
