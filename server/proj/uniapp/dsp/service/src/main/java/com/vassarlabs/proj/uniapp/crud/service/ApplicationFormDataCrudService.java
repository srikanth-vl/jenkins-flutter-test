package com.vassarlabs.proj.uniapp.crud.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationFormData;
import com.vassarlabs.proj.uniapp.dsp.repository.ApplicationFormDataRepository;
import com.vassarlabs.proj.uniapp.enums.ActiveFlags;

@Component
public class ApplicationFormDataCrudService {
	
	@Autowired private ApplicationFormDataRepository repository;
	
	public void insertApplicationFormData(ApplicationFormData data) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertListOfApplicationFormData(List<ApplicationFormData> data) 
    		throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }

    public List<ApplicationFormData> findApplicationFormDataByPartitionKey(UUID superAppId, UUID applicationId, ActiveFlags activeFlag) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return filterActiveInActiveForms(repository.findByPartitionKey(superAppId, applicationId), activeFlag);
    }
    
    public ApplicationFormData findApplicationFormDataByPrimaryKey(UUID superAppId, UUID applicationId, UUID projectId, int formType,
    		int versionNumber, ActiveFlags active)
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return filterActiveInActiveForms(Arrays.asList(repository.findByPrimaryKey(superAppId, applicationId, projectId, formType, versionNumber)), active).stream().findFirst().orElse(null);
    }
    
    public List<ApplicationFormData> findLatestApplicationFormData(UUID superAppId, UUID applicationId, Set<UUID> projectIds, List<Integer> assignedActions, ActiveFlags active) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return filterActiveInActiveForms(repository.findLatestApplicationData(superAppId, applicationId, new ArrayList<>(projectIds), assignedActions), active);
    }

	public ApplicationFormData findLatestVersionAppFormData(UUID superAppId, UUID appId, UUID projectId, Integer formType, ActiveFlags active) {
		ApplicationFormData appFormData = repository.findLatestVersionFormData(superAppId, appId, projectId, formType);
		if(appFormData == null) {
			return null;
		} else {
			List<ApplicationFormData> appFormList = Arrays.asList(appFormData);
			return filterActiveInActiveForms(appFormList, active).stream().findFirst().orElse(null);
		}
	}
	
	public List<ApplicationFormData> findAllAppFormDataForAProject(UUID superAppId, UUID appId, UUID projectId, ActiveFlags active) {
		List<ApplicationFormData> formDataList = repository.findAllFormsDataForProjects(superAppId, appId, Arrays.asList(projectId));
		return filterActiveInActiveForms(formDataList, active);
	}
	
	private List<ApplicationFormData> filterActiveInActiveForms(List<ApplicationFormData> applicationFormDataList, ActiveFlags activeFlag){
		
		List<ApplicationFormData> forms = new ArrayList<ApplicationFormData>();
    	
    	if (applicationFormDataList == null || applicationFormDataList.isEmpty()) 
    		return forms;
    	
    	if(activeFlag == ActiveFlags.ALL)
    		return applicationFormDataList;
    	
    	return applicationFormDataList.stream().filter(i -> i.getActiveFlag() == activeFlag.getValue()).collect(Collectors.toList());
    	
	}
}
