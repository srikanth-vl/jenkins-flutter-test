package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.TupleValue;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;

@Repository
public interface FieldMetaDataRepository 
	extends CrudRepository<FieldMetaData, String>{
	
	@Query("SELECT * from field_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1")
	List<FieldMetaData> findByPartitionKey(UUID superAppId, UUID appId);
	
	@Query("SELECT * from field_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id = ?2"
			+ " AND form_type = ?3"
			+ " AND md_version = ?4"
			+ " AND key_type = ?5"
			+ " AND key = ?6")
	FieldMetaData findByPrimaryKey(UUID superAppId, UUID appId, UUID projectId, int formType, int metaDataVersion, int keyType, String key);
	
	@Query("SELECT * from field_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND (project_id, form_type, md_version) IN ?2")
	List<FieldMetaData> findAllFieldsOfProjects(UUID superAppId, UUID appId, List<TupleValue> projectIdTuples);
	
	@Query("SELECT * from field_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND (project_id, form_type) IN ?2")
	List<FieldMetaData> findAllFieldsOfProjectsWithoutVersion(UUID superAppId, UUID appId, List<TupleValue> projectIdTuples);
	
	
	@Query("SELECT * from field_meta_Data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id = ?2"
			+ " AND form_type = ?3"
			+ " LIMIT 1")
	FieldMetaData findLatestMetaVersion(UUID superAppId, UUID appId, UUID projectId, int formType);
	
	@Query("SELECT * from field_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id = ?2"
			+ " AND form_type = ?3"
			+ " AND md_version = ?4")
	List<FieldMetaData> findFieldMetaData(UUID superAppId, UUID appId, UUID projectId, int formType, int metaVersion);
	
	@Query("SELECT * from field_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id IN ?2"
			+ " AND form_type = ?3")
	List<FieldMetaData> findLatestFieldMetaDataForProjects(UUID superAppId, UUID appId, List<UUID> projectIdList, int formType);
}
