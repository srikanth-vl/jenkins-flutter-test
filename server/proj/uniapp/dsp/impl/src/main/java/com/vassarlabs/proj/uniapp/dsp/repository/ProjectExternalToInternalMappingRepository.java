package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;

@Repository
public interface ProjectExternalToInternalMappingRepository extends CrudRepository<ProjectExternalInternalMapData, String> {

		@Query("SELECT * from project_external_to_internal_mapping"
				+ " WHERE super_app_id = ?0"
				+ " AND app_id = ?1")
		List<ProjectExternalInternalMapData> findByPartitionKey(UUID superAppId, UUID applicationId);
		
		@Query("SELECT * from project_external_to_internal_mapping"
				+ " WHERE super_app_id = ?0"
				+ " AND app_id = ?1"
				+ " AND proj_ext_id = ?2"
				+ " AND project_id = ?3")
		ProjectExternalInternalMapData findByPrimaryKey(UUID superAppId, UUID applicationId, String projectExternalId, UUID projectId);

		@Query("SELECT * from project_external_to_internal_mapping"
				+ " WHERE super_app_id = ?0"
				+ " AND app_id = ?1"
				+ " AND proj_ext_id = ?2")
		ProjectExternalInternalMapData findProjectExternalInternalMapDataForProjectExternalId(UUID superAppId, UUID applicationId, String projectExternalId);

		@Query("SELECT *"
				+ " FROM project_external_to_internal_mapping"
				+ " WHERE super_app_id = ?0"
				+ " AND app_id = ?1"
				+ " AND proj_ext_id IN ?2")
		List<ProjectExternalInternalMapData> findAllProjectExternalInternalMapDataForProjectExternalIds(UUID superAppId, UUID applicationId, List<String> projectExternalIds);

		@Query("SELECT * from project_internal_to_external_mapping_view"
				+ " WHERE super_app_id = ?0"
				+ " AND app_id = ?1"
				+ " AND project_id = ?2")
		ProjectExternalInternalMapData findProjectExternalInternalMapDataForProjectInternalId(UUID superAppId, UUID applicationId, UUID projectId);

		@Query("SELECT *"
				+ " FROM project_internal_to_external_mapping_view"
				+ " WHERE super_app_id = ?0"
				+ " AND app_id = ?1"
				+ " AND project_id IN ?2")
		List<ProjectExternalInternalMapData> findAllProjectExternalInternalMapDataForProjectIds(UUID superAppId, UUID applicationId, List<UUID> projectIds);

		@Query("DELETE from project_external_to_internal_mapping"
				+ " WHERE super_app_id = ?0"
				+ " and app_id = ?1")
		void deleteAllRecords(UUID superAppId, UUID appId);
		

		@Query("DELETE from project_external_to_internal_mapping"
				+ " WHERE super_app_id = ?0"
				+ " AND app_id = ?1"
				+ " AND proj_ext_id IN ?2")
		void deleteFromProjectExternalIds(UUID superAppId, UUID appId, List<String> projectExternalIds);

}