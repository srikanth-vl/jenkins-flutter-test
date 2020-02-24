package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.EntityMetaData;

@Repository
public interface EntityMetadataRepository 
	extends CrudRepository<EntityMetaData, String> {
	
	@Query("SELECT * from entity_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1")
	List<EntityMetaData> findByPartitionKey( UUID superAppId, UUID applicationId);
	
	@Query("SELECT * from entity_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2"
			+ " and user_id = ?3"
			+ " and parent_entity = ?4"
			+ " and entity_name = ?5")
	List<EntityMetaData> findByPrimaryKey( UUID superAppId, UUID applicationId, UUID projectId, String userId, String parent, String name );

	@Query("SELECT * from entity_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2"
			+ " and user_id = ?3"
			+ " and parent_entity = ?4"
			+ " and entity_name = ?5"
			+ " LIMIT 1")
	EntityMetaData findLatestEntityMetadata(UUID superAppId, UUID applicationId, UUID projectId, String userId, String parent, String name );
	@Query("SELECT super_app_id, app_id, project_id, user_id, max(insert_ts) as insert_ts, parent_entity, entity_name, elements"
			+ " from entity_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " GROUP BY"
			+ " super_app_id, app_id, project_id,user_id,parent_entity,entity_name")
	List<EntityMetaData> findLatestEntityMetadataForApp(UUID superAppId, UUID applicationId);
	@Query("SELECT super_app_id, app_id, project_id, user_id, max(insert_ts) as insert_ts, parent_entity, entity_name, elements"
			+ " from entity_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id =?2"
			+ " GROUP BY"
			+ " super_app_id, app_id, project_id,user_id,parent_entity,entity_name")
	List<EntityMetaData> findLatestEntityMetadataForProject(UUID superAppId, UUID applicationId,UUID projectId);
	
	@Query("SELECT super_app_id, app_id, project_id, user_id, max(insert_ts) as insert_ts, parent_entity, entity_name, elements"
			+ " from entity_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id =?2"
			+ " and user_id =?3"
			+ " GROUP BY"
			+ " super_app_id, app_id, project_id,user_id,parent_entity,entity_name")
	List<EntityMetaData> findLatestEntityMetadataForUser(UUID superAppId, UUID applicationId, UUID projectId, String userId );
}
