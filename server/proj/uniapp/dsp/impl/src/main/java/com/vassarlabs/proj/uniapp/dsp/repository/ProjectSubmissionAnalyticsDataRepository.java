package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectSubmissionAnalyticsData;

@Repository
public interface ProjectSubmissionAnalyticsDataRepository 
	extends CrudRepository<ProjectSubmissionAnalyticsData, String>{
	
	@Query("SELECT * from project_submission_analytics"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND parent_entity = ?2")
	List<ProjectSubmissionAnalyticsData> findByPartitionKey(UUID superApp, UUID applicationId, String parentEntity);
	
	
	@Query("SELECT * from project_submission_analytics"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND parent_entity = ?2"
			+ " AND date = ?3"
			+ " AND entity = ?4")
	ProjectSubmissionAnalyticsData findByPrimaryKey(UUID superApp, UUID applicationId, String parentEntity, int date, String childEntity);
	
	@Query("SELECT * from project_submission_analytics"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND parent_entity = ?2"
			+ " AND date > ?3")
	List<ProjectSubmissionAnalyticsData> findSubmissionCountForGivenParenEntity(UUID superApp, UUID applicationId, String parentEntity, int date);

}