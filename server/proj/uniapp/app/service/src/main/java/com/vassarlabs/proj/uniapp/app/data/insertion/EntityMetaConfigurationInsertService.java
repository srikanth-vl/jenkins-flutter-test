package com.vassarlabs.proj.uniapp.app.data.insertion;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.Entity;
import com.vassarlabs.proj.uniapp.api.pojo.EntityElement;
import com.vassarlabs.proj.uniapp.api.pojo.EntityGroup;
import com.vassarlabs.proj.uniapp.api.pojo.EntityMetaConfigInsertObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.EntityMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.EntityMetadataCrudService;

@Component
public class EntityMetaConfigurationInsertService {

ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired protected IVLLogService logFactory;
	@Autowired protected EntityMetadataCrudService entityMetadataCrudService;
	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	
	public void insertEntityMetaConfig(EntityMetaConfigInsertObject entityMeta) {
		Map<String, Map<String, Set<String>>>configMap = new HashMap<>();
		configMap  = getExistingEntity(entityMeta.getSuperAppId());
		List<EntityGroup> entityGroups =  entityMeta.getEntityConfig();
		String parentEntity = entityMeta.getEntityParent();;
		if(entityMeta.getEntityParent() == null || entityMeta.getEntityParent().isEmpty()) {
			parentEntity = "DEFAULT";
		} 
		insertEntityMetaConfiguration(entityGroups, parentEntity, configMap);
		uploadToDB(configMap, entityMeta.getSuperAppId(), entityMeta.getAppId());
		

	}
	
	public void insertEntityMetaConfiguration(List<EntityGroup> groups, String parent, Map<String, Map<String, Set<String>>>configMap) {
		
		if(groups == null || groups.isEmpty()) {
			return;
		}
		for(EntityGroup entityGroup : groups) {
			updateEntityConfigMap(entityGroup, parent, configMap);
		}
	}
	
	public void updateEntityConfigMap(EntityGroup entityGroup, String parent, Map<String, Map<String, Set<String>>>configMap) {
		Map<String,Set<String>> typeToElementsMap = configMap.get(parent);
		Set<String> elements =  new HashSet<>();
		if(typeToElementsMap == null) {
			typeToElementsMap =  new HashMap<>();
		}
		String type =  entityGroup.getType(); 
		elements = typeToElementsMap.get(type);
		if(elements == null) {
			elements = new HashSet<>();
		}
		for (Entity entity: entityGroup.getEntities()) {
			elements.add(entity.getName());
			List<EntityGroup> childEntityGroups = entity.getEntityGroups();
			if(childEntityGroups != null && !childEntityGroups.isEmpty()) {		
				insertEntityMetaConfiguration(childEntityGroups, parent +"##"+ entityGroup.getType() +"$$"+entity.getName(), configMap);
			} 
		}
		typeToElementsMap.put(type, elements);
		configMap.put(parent, typeToElementsMap);
	}
	public EntityElement getEntityElement (String name) {
		EntityElement element =  new EntityElement(name);
		return element;
	}
	public void uploadToDB(Map<String, Map<String,Set<String>>> configMap, UUID superApp, UUID app) {
		List<EntityMetaData> entityDbData = new ArrayList<>();
		for(String parent : configMap.keySet()) {
			Map<String, Set<String>> typetoElementsMap= configMap.get(parent);
			for(String type : typetoElementsMap.keySet()) {
				EntityMetaData data = new  EntityMetaData();
				data.setApplicationId(app);
				data.setSuperAppId(superApp);
				data.setEntityName(type);
				data.setParentEntity(parent);
				data.setUserId(CommonConstants.DEFAULT_USER_ID);
				data.setInsertTs(System.currentTimeMillis());
				data.setProjectId(UUIDUtils.getDefaultUUID());
				List<EntityElement> elements = getElements(typetoElementsMap.get(type));
				try {
					String typeToElementsStr = objectMapper.writeValueAsString(elements);
					data.setElements(typeToElementsStr);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entityDbData.add(data);
			}	
		}
		if(entityDbData != null && !entityDbData.isEmpty()) {
			entityMetadataCrudService.insertApplicationDBData(entityDbData); }
	}
	public List<EntityElement> getElements(Set<String> values) {
		List<EntityElement> elements = new ArrayList<>();
		for (String value : values) {
			EntityElement element=  new EntityElement();
			element.setName(value);
			elements.add(element);
		}
		return elements;
	}
	public Map<String, Map<String, Set<String>>> getExistingEntity(UUID superapp) {
		Map<String, Map<String, Set<String>>> configMap =  new HashMap<>();
		try {
			List<EntityMetaData> data = entityMetadataCrudService.getLatestEntityMetadataForApp(superapp, UUIDUtils.getDefaultUUID());
			for (EntityMetaData entityData : data) {
				String type = entityData.getEntityName();
				String parent = entityData.getParentEntity();
				List<EntityElement> elements = entityData.getElements() != null && !entityData.getElements().isEmpty() ? 
						objectMapper.readValue(entityData.getElements(), new TypeReference<List<EntityElement>>() {}) :new ArrayList<>();
					for (EntityElement ele  : elements) {
						updateEntityConfigMap(configMap,ele.getName(),parent, type);
					}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return configMap;
	}
	public void updateEntityConfigMap(Map<String, Map<String,Set<String>>> entityConfigMap, String value, String parent, String type) {
		Map<String,Set<String>> typeToElementsMap = entityConfigMap.get(parent);
		Set<String> elements = new HashSet<>();
		if(typeToElementsMap == null) {
			typeToElementsMap =  new HashMap<String, Set<String>>();
		}

		elements = typeToElementsMap.get(type);
		if(elements == null) {
			elements = new HashSet<>();
		} 
		elements.add(value);
		typeToElementsMap.put(type, elements);
		entityConfigMap.put(parent, typeToElementsMap);
	}
}
