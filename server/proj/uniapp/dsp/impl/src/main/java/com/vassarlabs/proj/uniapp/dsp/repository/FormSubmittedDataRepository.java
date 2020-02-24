package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;

@Repository
public interface FormSubmittedDataRepository 
	extends CrudRepository<FormSubmitData, String> {
	
	@Query("SELECT super_app_id, app_id, project_id, date, key, value, form_instance_id," 
			+ " md_instance_id, user_id, user_type, blobAsBigint(timestampAsBlob(timestamp)) as timestamp, "
			+ "token_id, datatype, db_insert_ts, uom" 
			+ " from form_submission_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2")
	List<FormSubmitData> findByPartitionKey( UUID superAppId, UUID applicationId, UUID projectId );
	
	@Query("SELECT * from form_submission_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2"
			+ " and key = ?3"
			+ " and user_type = ?4"
			+ " and date = ?5"
			+ " and timestamp = ?6"
			+ " and user_id = ?7")
	FormSubmitData findByPrimaryKey( UUID superAppId, UUID applicationId, UUID projectId, String key, int userType, int date, long timestamp, long userId );

	@Query("SELECT * from form_submission_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2"
			+ " and key = ?3"
			+ " LIMIT 1")
	FormSubmitData findLatestSubmittedDataForaKey(UUID superAppId, UUID applicationId, UUID projectId, String key);
	
	@Query("SELECT super_app_id, app_id, project_id, max(date) as date, key, value, form_instance_id,"
			+ " md_instance_id, user_id, user_type, blobAsBigint(timestampAsBlob(timestamp)) as timestamp, token_id, datatype, db_insert_ts,uom"
			+ " from form_submission_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2"
			+ " GROUP BY"
			+ " super_app_id, app_id, project_id, key")
	List<FormSubmitData> findLatestValuesOfKeysForProject(UUID superAppId, UUID applicationId, UUID projectId);
	
	@Query("SELECT super_app_id, app_id, project_id, max(date) as date, key, value, form_instance_id,"
			+ " md_instance_id, user_id, user_type, blobAsBigint(timestampAsBlob(timestamp)) as timestamp, token_id, datatype, db_insert_ts,uom"
			+ " from form_submission_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id IN ?2"
			+ " GROUP BY"
			+ " super_app_id, app_id, project_id, key")
	List<FormSubmitData> findLatestValuesOfKeysForProjectList(UUID superAppId, UUID applicationId, List<UUID> projectIdList);
	
	@Query("SELECT super_app_id, app_id, project_id, date, key, value, form_instance_id,"
			+ " md_instance_id, user_id, user_type, blobAsBigint(timestampAsBlob(timestamp)) as timestamp, token_id, datatype, db_insert_ts,uom"
			+ " from form_submission_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2"
			+ " and key = ?3"
			+ " LIMIT ?4")
	List<FormSubmitData> findLastNValuesForKey(UUID superAppId, UUID applicationId, UUID projectId, String key, int n);
	
	@Query("SELECT super_app_id, app_id, project_id, date, key, value, form_instance_id,"
			+ " md_instance_id, user_id, user_type, blobAsBigint(timestampAsBlob(timestamp)) as timestamp, token_id, datatype, db_insert_ts,uom"
			+ " from form_submission_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id IN ?2")
	List<FormSubmitData> findValuesOfKeysForProjectList(UUID superAppId, UUID applicationId, List<UUID> projectIdList);

}
