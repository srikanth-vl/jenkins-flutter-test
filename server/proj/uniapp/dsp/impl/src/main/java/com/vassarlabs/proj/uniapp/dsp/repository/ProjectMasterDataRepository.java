package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.TupleValue;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;

@Repository
public interface ProjectMasterDataRepository 
	extends CrudRepository<ProjectMasterData, String>{
	
	@Query("SELECT * from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1")
	List<ProjectMasterData> findByPartitionKey(UUID superAppId, UUID applicationId);
	
	@Query("SELECT * from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2"
			+ " and date = ?3"
			+ " and key = ?4")
	ProjectMasterData findByPrimaryKey(UUID superAppId, UUID applicationId, UUID projectId, int date, String key);
	
	@Query("SELECT date from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " AND project_id = ?2"
			+ " LIMIT 1")
	Integer findLatestDateofProjectId(UUID superAppId, UUID applicationId, UUID projectId);
	
	@Query("SELECT super_app_id, app_id, project_id, max(date) as date from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " AND project_id IN ?2"
			+ " GROUP BY"
			+ " super_app_id, app_id, project_id")
	List<ProjectMasterData> findLatestDateOfProjectIds(UUID superAppId, UUID applicationId, List<UUID> projectIdList);
	
	@Query("SELECT * from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id = ?2"
			+ " AND date = ?3")
	List<ProjectMasterData> findProjectMasterDataByProjectId(UUID superAppId, UUID applicationId, UUID projectId, int date);
	
	@Query("SELECT project_id, max(date) AS date from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id IN ?2"
			+ " GROUP BY"
			+ " super_app_id, app_id, project_id")
	List<ProjectMasterData> findLatestDateForProject (UUID superAppId, UUID applicationId, List<UUID> projectIds);
	
	@Query("SELECT * from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND (project_id, date) IN ?2")
	List<ProjectMasterData> findProjectMasterDataByProjectIdAndDateTuple(UUID superAppId, UUID appId, List<TupleValue> projectIdDateTuples);
	
	@Query("SELECT * from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and (project_id, date) IN ?2")
	List<ProjectMasterData> findMasterDataForProjectIdAndDate(UUID superAppId, UUID applicationId, List<TupleValue> projectIdDateList);
	
	@Query("SELECT * from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and (project_id, date) IN ?2")
	List<ProjectMasterData> findMasterDataForProjectIdDateAndKey(UUID superAppId, UUID applicationId, List<TupleValue> projectIdDateAndKeyList);
	
	@Query("DELETE from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1")
	void deleteAllRecords(UUID superAppId, UUID appId);
	
	@Query("SELECT * from project_master_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1")
	List<ProjectMasterData> findAllProjectIdsForApp(UUID superAppId, UUID applicationId);

}