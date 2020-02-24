package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationFormData;

@Repository
public interface ApplicationFormDataRepository 
	extends CrudRepository<ApplicationFormData, String> {

	@Query("SELECT * from app_form"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1")
	List<ApplicationFormData> findByPartitionKey(UUID superAppId, UUID applicationId);
	
	@Query("SELECT * from app_form"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id = ?2"
			+ " AND form_type = ?3"
			+ " AND form_version_number = ?4")
	ApplicationFormData findByPrimaryKey(UUID superAppId, UUID applicationId, UUID projectId, int formType, int versionNumber);
	
	@Query(" select super_app_id, app_id, project_id, form_type, max(form_version_number) AS form_version_number, is_active, form_instance_id, form_json, md_instance_id"
			+ " FROM app_form"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id IN ?2"
			+ " AND form_type IN ?3"
			+ " GROUP BY super_app_id,app_id,project_id, form_type")
	List<ApplicationFormData> findLatestApplicationData(UUID superAppId, UUID applicationId, List<UUID> projectIdList, List<Integer> formTypes);

	@Query(" select *"
			+ " FROM app_form"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id IN ?2")
	List<ApplicationFormData> findAllFormsDataForProjects(UUID superAppId, UUID appId, List<UUID> projectList);

	@Query("SELECT * from app_form"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id = ?2"
			+ " AND form_type = ?3"
			+ " LIMIT 1")
	ApplicationFormData findLatestVersionFormData(UUID superAppId, UUID appId, UUID projectId, Integer formType);
}
