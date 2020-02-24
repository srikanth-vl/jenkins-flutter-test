package com.vassarlabs.proj.uniapp.crud.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.dsp.repository.ProjectExternalToInternalMappingRepository;

@Component
public class ProjectExternalToInternalMappingCrudService {

	@Autowired private ProjectExternalToInternalMappingRepository repository;
	
	public void insertProjectExternalInternalMapData(ProjectExternalInternalMapData data) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertListOfProjectExternalInternalMapData(List<ProjectExternalInternalMapData> data) 
    		throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }

	public List<ProjectExternalInternalMapData> findProjectExternalInternalMapDataByPartitionKey(UUID superAppId, UUID applicationId) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		return repository.findByPartitionKey(superAppId, applicationId);
	}
	
    public ProjectExternalInternalMapData findProjectExternalInternalMapDataByPrimaryKey(UUID superAppId, UUID appId, String projectExternalId, UUID projectID) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPrimaryKey(superAppId, appId, projectExternalId, projectID);
    }
    
    public ProjectExternalInternalMapData findProjectExternalInternalMapDataForProjectId(UUID superAppId, UUID appId, String projectExternalId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findProjectExternalInternalMapDataForProjectExternalId(superAppId, appId, projectExternalId);
    }
    
    public List<ProjectExternalInternalMapData> findProjectExternalInternalListDataForProjectExternalIds(UUID superAppId, UUID appId, List<String> projectExternalIds) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findAllProjectExternalInternalMapDataForProjectExternalIds(superAppId, appId, projectExternalIds);
    }
    
    public Map<String, ProjectExternalInternalMapData> findProjectExternalInternalMapDataForProjectExternalIds(UUID superAppId, UUID appId, List<String> projectExternalIds) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	List<ProjectExternalInternalMapData> externalInternalListData = repository.findAllProjectExternalInternalMapDataForProjectExternalIds(superAppId, appId, projectExternalIds);
    	Map<String, ProjectExternalInternalMapData>  externalIdToObjectMap = externalInternalListData.stream().collect(Collectors.toMap(ProjectExternalInternalMapData::getProjectExternalId, Function.identity()));
    	return externalIdToObjectMap;
    }

    public ProjectExternalInternalMapData findProjectExternalInternalMapDataForProjectId(UUID superAppId, UUID appId, UUID projectId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findProjectExternalInternalMapDataForProjectInternalId(superAppId, appId, projectId);
    }
    
    public List<ProjectExternalInternalMapData> findProjectExternalInternalMapDataForProjectIIds(UUID superAppId, UUID appId, List<UUID> projectIds) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findAllProjectExternalInternalMapDataForProjectIds(superAppId, appId, projectIds);
    }
    
	public void deleteAllRecords(UUID superAppId, UUID appId) {
		repository.deleteAllRecords(superAppId, appId);
	}

	public List<UUID> findAllProjectsInApp(UUID superAppId, UUID appId) {
		List<ProjectExternalInternalMapData> projectExternalToInternalMapData = repository.findByPartitionKey(superAppId, appId);
		if(projectExternalToInternalMapData != null && !projectExternalToInternalMapData.isEmpty()) {
			return projectExternalToInternalMapData.stream().map(ProjectExternalInternalMapData::getProjectId).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}