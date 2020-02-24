package com.vassarlabs.proj.uniapp.entitymetadataconfig.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.EntityMetadataConfigRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.IEntityMetadataConfigurationService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.EntityMetaData;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.EntityMetadataConfigurationConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.EntityMetadataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;

@Component
public class EntityMetadataConfigurationService implements IEntityMetadataConfigurationService	 {
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
	@Override
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
		String userId = requestObject.getEntityMetadtaUserId();
		String parentEntity = requestObject.getParentEntity();
		String entityName= requestObject.getEntityName();
		Long appMaxInsertTs = requestObject.getLastSyncTs();
		if(projectId == null ) {
			entityConfigList = entityMetadataCrudService.getLatestEntityMetadataForApp(superAppId, appId);
		} else if(userId == null ) {
			entityConfigList = entityMetadataCrudService.getLatestEntityMetadataForProject(superAppId, appId, projectId);
		} else if(parentEntity == null || entityName == null ){
			entityConfigList = entityMetadataCrudService.getLatestEntitiesMetadataForUser(superAppId, appId, projectId, userId);
		} else if(parentEntity != null && entityName != null && !entityName.isEmpty() ) {
			EntityMetaData data  = entityMetadataCrudService.getLatestEntityMetadata(superAppId, appId, projectId, userId, parentEntity, entityName);
			if(data != null) {
				entityConfigList.add(data); 
			}
		} 
		List<EntityMetaData> deltaEntityConfigList =  new ArrayList<>(); 
		for (EntityMetaData entityMetaData : entityConfigList) {
			if(entityMetaData.getInsertTs() > appMaxInsertTs) {
				deltaEntityConfigList.add(entityMetaData);
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

}
