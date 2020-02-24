package com.vassarlabs.proj.uniapp.crud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;
import com.vassarlabs.proj.uniapp.dsp.repository.FormSubmittedDataRepository;

@Component
public class FormSubmittedDataCrudService {
	
	@Autowired private FormSubmittedDataRepository repository;
	
	public void insertFormSubmittedData(FormSubmitData data) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		repository.save(data);
	}
	
	public void insertListOfFormSubmittedData(List<FormSubmitData> data) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		repository.saveAll(data);
	}
	
	public List<FormSubmitData> findFormSubmittedDataByPartitionKey(UUID superAppId, UUID applicationId, UUID projectId) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		return repository.findByPartitionKey(superAppId, applicationId, projectId);
	}
	
	public FormSubmitData findFormSubmittedDataByPrimaryKey(UUID superAppId, UUID applicationId, UUID projectId,
			int userType, int date, long timestamp, String key, long userId) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		return repository.findByPrimaryKey(superAppId, applicationId, projectId, key, userType, date, timestamp,  userId);
	}
	

	public FormSubmitData findLatestSubmittedDataForaKey(UUID superAppId, UUID applicationId, UUID projectId, String key) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		return repository.findLatestSubmittedDataForaKey(superAppId, applicationId, projectId, key);
	}
	
	public List<FormSubmitData> findLatestValuesOfKeys(UUID superAppId, UUID applicationId, UUID projectId) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		return repository.findLatestValuesOfKeysForProject(superAppId, applicationId, projectId);
	}
	
	public Map<UUID, Map<String, FormSubmitData>> findLatestFormSubmittedDataOfKeys(UUID superAppId, UUID applicationId, List<UUID> projectIds) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		
		Map<UUID, Map<String, FormSubmitData>> projectIdToFormSubmittedData = new HashMap<>();
		List<FormSubmitData> formSubmittedDataList = repository.findLatestValuesOfKeysForProjectList(superAppId, applicationId, projectIds);
		
		for(FormSubmitData formSubmittedData : formSubmittedDataList) {
			if(projectIdToFormSubmittedData.get(formSubmittedData.getProjectId()) == null)
				projectIdToFormSubmittedData.put(formSubmittedData.getProjectId(), new HashMap<>());
			projectIdToFormSubmittedData.get(formSubmittedData.getProjectId()).put(formSubmittedData.getKey(), formSubmittedData);
		}
		return projectIdToFormSubmittedData;
	}
	
	public List<FormSubmitData> getLastNDataForKey(UUID superAppId, UUID appId, UUID projectId, String key, int n) {
		return repository.findLastNValuesForKey(superAppId, appId, projectId, key, n);
	}
	public Map<UUID, List<FormSubmitData>> findFormSubmittedDataForListOfProjects(UUID superAppId, UUID applicationId, List<UUID> projectIds) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		
		Map<UUID, List<FormSubmitData>> projectIdToFormSubmittedData = new HashMap<>();
		List<FormSubmitData> formSubmittedDataList = repository.findValuesOfKeysForProjectList(superAppId, applicationId, projectIds);
		
		for(FormSubmitData formSubmittedData : formSubmittedDataList) {
			if(projectIdToFormSubmittedData.get(formSubmittedData.getProjectId()) == null) {
				projectIdToFormSubmittedData.put(formSubmittedData.getProjectId(),new ArrayList<> ());
			}
			projectIdToFormSubmittedData.get(formSubmittedData.getProjectId()).add( formSubmittedData);
		}
		return projectIdToFormSubmittedData;
	}
}
