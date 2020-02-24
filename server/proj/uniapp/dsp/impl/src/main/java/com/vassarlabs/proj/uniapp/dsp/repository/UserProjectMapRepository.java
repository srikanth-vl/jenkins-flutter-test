package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapDBData;

@Repository
public interface UserProjectMapRepository
	extends CrudRepository<UserProjectMapDBData, String>{
	
	@Query("SELECT * from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1")
	List<UserProjectMapDBData> findByPartitionKey(UUID superAppId, UUID appId);
	
	@Query("SELECT * from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id = ?2"
			+ " AND user_type = ?3")
	List<UserProjectMapDBData> findByPrimaryKey(UUID superAppId, UUID appId, String userId, int userType);

	@Query("SELECT * from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id = ?2")
	List<UserProjectMapDBData> findByUserId(UUID superAppId, UUID appId, String userId);
	
	@Query("DELETE from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1")
	void deleteAllRecords(UUID superAppId, UUID appId);
	
	@Query("DELETE from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and user_id IN  ?2"
			+ " and user_type = ?3")
	void deleteAllRecordsForUsersWithUserType(UUID superAppId, UUID appId, List<String> userIdList, int userType);
	
	@Query("DELETE from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and user_id =  ?2")
	void deleteAllRecordsForAUser(UUID superAppId, UUID appId, String userId);
	
	@Query("SELECT * from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id IN ?2")
	List<UserProjectMapDBData> getAllAssignedProjectForApp(UUID superAppId, UUID appId, List<String> userIds);
	
	@Query("DELETE from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id =  ?2")
	void deleteAllRecordsForAProject(UUID superAppId,UUID appIds, UUID projectId);
	
	@Query("SELECT * from project_to_user_mapping"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id IN ?2")
	List<UserProjectMapDBData> getAllAssignedUsersForListOfProjects(UUID superAppId, UUID appId, List<UUID> projectIds);
		
	@Query("DELETE from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and user_id IN  ?2")
	void deleteAllRecordsForGivenUsersForApp(UUID superAppId, UUID appId, List<String> userIds);
	
	@Query("DELETE from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and user_id IN  ?2"
			+ " and user_type = ?3"
			+ " and project_id =  ?4")
	void deleteAllRecordsForUsersWithUserTypeAndGivenProject(UUID superAppId, UUID appId, List<String> userIdList, int userType, UUID projectID);
	@Query("DELETE from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and user_id IN  ?2"
			+ " and user_type IN ?3"
			+ " and project_id =  ?4")
	void deleteAllRecordsForUsersAndGivenProject(UUID superAppId, UUID appId, List<String> userIdList, List<Integer> userPriorities, UUID projectID);
	
	@Query("DELETE from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and user_id IN ?2"
			+ " and user_type IN ?3"
			+ " and project_id IN ?4")
	void deleteAllRecordsForGivenProjects(UUID superAppId, UUID appId, List<String> userIds, List<Integer> userPriorities,  List<UUID> projectIds);

	@Query("SELECT * from user_project_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1")
	List<UserProjectMapDBData> countOfProjectsAssignedToUsers(UUID superAppId, UUID appId);
}