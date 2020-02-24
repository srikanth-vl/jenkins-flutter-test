package com.vassarlabs.proj.uniapp.crud.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.TupleType;
import com.datastax.driver.core.TupleValue;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.dsp.repository.ProjectMasterDataRepository;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;

@Component
public class ProjectMasterDataCrudService {

	@Autowired ProjectMasterDataRepository repository;
	@Autowired protected IVLLogService logFactory;
	protected IVLLogger logger;
	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}


	public void insertProjectMasterData(ProjectMasterData data) 
			throws CassandraConnectionFailureException,	CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.save(data);
	}

	public void insertListOfProjectMasterData(List<ProjectMasterData> data) 
			throws CassandraConnectionFailureException,	CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException,	CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.saveAll(data);
	}

	public List<ProjectMasterData> findProjectMasterDataByPartitionKey(UUID superAppId, UUID applicationId, ProjectStates state) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		return filterProjectMasterdataForGivenState(repository.findByPartitionKey(superAppId, applicationId), state);
	}

	public ProjectMasterData findProjectMasterDataByPrimaryKey(UUID superAppId, UUID applicationId, UUID projectId, int date, String key, ProjectStates state) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException,	CassandraQuerySyntaxException, CassandraTypeMismatchException {
		ProjectMasterData data = repository.findByPrimaryKey(superAppId, applicationId, projectId, date, key);
		if(data == null)
			return null;
		return filterProjectMasterdataForGivenState(Arrays.asList(data), state).stream().findFirst().orElse(null);
	}
	
	public int findLatestDateofProjectId (UUID superAppId, UUID applicationId, UUID projectId, ProjectStates state) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		if(state == ProjectStates.ALL || 
				!filterProjectIds(superAppId, applicationId, Arrays.asList(projectId), state).isEmpty()) {
			return repository.findLatestDateofProjectId(superAppId, applicationId, projectId); }
		else 
			return -1;
	}
	
	public List<ProjectMasterData> findProjectMasterDataByProjectId(UUID superAppId, UUID applicationId, UUID projectId, int date, ProjectStates state)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		if(state == ProjectStates.ALL || 
				!filterProjectIds(superAppId, applicationId, Arrays.asList(projectId), state).isEmpty()) {
			return repository.findProjectMasterDataByProjectId(superAppId, applicationId, projectId, date);
		}
		else 
			return new ArrayList<ProjectMasterData>();
	}
	
	public Map<UUID, Integer> findProjectToLatestDateMap (UUID superAppId, UUID applicationId, List<UUID> projectIds, List<ProjectStates> states) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		List<UUID> ids = filterProjectIds(superAppId, applicationId, projectIds, states);
		Map<UUID, Integer> projectIdToLatestDateMap = new HashMap<>();
		if(ids.isEmpty())
			return projectIdToLatestDateMap;
		List<ProjectMasterData> masterDataList = repository.findLatestDateForProject(superAppId, applicationId, ids);
		projectIdToLatestDateMap = masterDataList.stream().collect(Collectors.toMap(ProjectMasterData :: getProjectId, ProjectMasterData :: getDate));
		return projectIdToLatestDateMap;
	}
	
	public List<ProjectMasterData> findDataForProjectIdAndDate(UUID superAppId, UUID applicationId, Map<UUID, Integer> projectIdToLatestDateMap, List<ProjectStates> states) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		List<UUID> projectIds  = new ArrayList<UUID> ();
		projectIds.addAll(projectIdToLatestDateMap.keySet());
		List<UUID> filteredIds = filterProjectIds(superAppId, applicationId, projectIds, states);
		List<TupleValue> projectTuples = new ArrayList<>();
		if(filteredIds.isEmpty()) {
			return new ArrayList<ProjectMasterData>();
		}
		for(UUID projID : projectIdToLatestDateMap.keySet()) {
			if(!filteredIds.contains(projID))
				continue;
			TupleType tupleType = TupleType.of(com.datastax.driver.core.ProtocolVersion.NEWEST_SUPPORTED, 
					com.datastax.driver.core.CodecRegistry.DEFAULT_INSTANCE,
					com.datastax.driver.core.DataType.uuid(), com.datastax.driver.core.DataType.cint());
			TupleValue tupleValue =
				    tupleType.newValue().setUUID(0, projID).setInt(1, projectIdToLatestDateMap.get(projID));
			TupleValue tupleValueForDefaultDate =
				    tupleType.newValue().setUUID(0, projID).setInt(1, CommonConstants.DEFAULT_DATE);
			projectTuples.add(tupleValue);
			projectTuples.add(tupleValueForDefaultDate);
		}
		return repository.findMasterDataForProjectIdAndDate(superAppId, applicationId, projectTuples);
	}
	
	public Map<UUID, ProjectMasterData> findDataForProjectsState(UUID superAppId, UUID applicationId, List<UUID> projectIds) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		
		List<TupleValue> projectTuples = new ArrayList<>();
		List<ProjectMasterData> projectMasterDataList = new ArrayList<>();
		Map<UUID, ProjectMasterData> projectIdToStateMap = new HashMap<>();
		
		if(projectIds == null || projectIds.isEmpty())
			return new HashMap<>();
					
		for(UUID projID : projectIds) {
			TupleType tupleType = TupleType.of(com.datastax.driver.core.ProtocolVersion.NEWEST_SUPPORTED, 
					com.datastax.driver.core.CodecRegistry.DEFAULT_INSTANCE,
					com.datastax.driver.core.DataType.uuid(), com.datastax.driver.core.DataType.cint());
			TupleValue tupleValueForDefaultDate =
				    tupleType.newValue().setUUID(0, projID).setInt(1, CommonConstants.DEFAULT_DATE);
			projectTuples.add(tupleValueForDefaultDate);
		}
		
		projectMasterDataList = repository.findMasterDataForProjectIdDateAndKey(superAppId, applicationId, projectTuples);
		projectMasterDataList = projectMasterDataList.stream().filter(i -> i.getKey().equals(MasterDataKeyNames.STATE_KEY)).collect(Collectors.toList());
		
		for(ProjectMasterData projectMasterData : projectMasterDataList) {
			projectIdToStateMap.put(projectMasterData.getProjectId(), projectMasterData);
		}
		
		return projectIdToStateMap;
		
	}
	
	public List<ProjectMasterData> findDataForProjectIdsAndDefaultDate(UUID superAppId, UUID applicationId, List<UUID> projectIds, ProjectStates state) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		List<UUID> ids = filterProjectIds(superAppId, applicationId, projectIds, state);
		if(ids.isEmpty())
			return new ArrayList<>();
		List<TupleValue> projectTuples = new ArrayList<>();
		for(UUID projID : ids) {
			TupleType tupleType = TupleType.of(com.datastax.driver.core.ProtocolVersion.NEWEST_SUPPORTED, 
					com.datastax.driver.core.CodecRegistry.DEFAULT_INSTANCE,
					com.datastax.driver.core.DataType.uuid(), com.datastax.driver.core.DataType.cint());
			TupleValue tupleValueForDefaultDate =
				    tupleType.newValue().setUUID(0, projID).setInt(1, CommonConstants.DEFAULT_DATE);
			projectTuples.add(tupleValueForDefaultDate);
		}
		return repository.findMasterDataForProjectIdAndDate(superAppId, applicationId, projectTuples);
	}
	
	/**
	 * Returns map of master data key -> latest value
	 * @param superAppId
	 * @param appId
	 * @param projectId
	 * @return
	 */
	public Map<String, ProjectMasterData> getKeyToTargetValue(UUID superAppId, UUID appId, UUID projectId, ProjectStates state) {
		
		Map<String, ProjectMasterData> keyToTargetValueMap = new HashMap<String, ProjectMasterData>();
		List<UUID> ids = filterProjectIds(superAppId, appId, Arrays.asList(projectId), state);
		if(ids.isEmpty())
			return keyToTargetValueMap;
		Integer latestDate = repository.findLatestDateofProjectId(superAppId, appId, projectId);
		if(latestDate == null) {
			logger.info("No data found for super app "+ superAppId+ " app "+ appId+ "project "+ projectId);
			return new HashMap<String, ProjectMasterData>();
		}
		List<ProjectMasterData> masterDataList = repository.findProjectMasterDataByProjectId(superAppId, appId, projectId, latestDate);
		for(ProjectMasterData masterData : masterDataList) {
			keyToTargetValueMap.put(masterData.getKey(), masterData);
		}
		return keyToTargetValueMap;
	}

	/**
	 * Returns map of master data key -> latest value
	 * @param superAppId
	 * @param appId
	 * @param projectId
	 * @return
	 */
	public Map<UUID, Map<String, ProjectMasterData>> getAllMasterDataForProjectIds(UUID superAppId, UUID appId, List<UUID> projectIds) {
		
		Map<UUID, Map<String, ProjectMasterData>> keyToTargetValueMap = new HashMap<>();
		List<ProjectMasterData> latestDateForProjectIds = repository.findLatestDateOfProjectIds(superAppId, appId, projectIds);
		List<TupleValue> tupleValues = new ArrayList<>();
		
		for (ProjectMasterData projectMasterData : latestDateForProjectIds) {
			TupleType tupleType = TupleType.of(com.datastax.driver.core.ProtocolVersion.NEWEST_SUPPORTED, 
					com.datastax.driver.core.CodecRegistry.DEFAULT_INSTANCE,
					com.datastax.driver.core.DataType.uuid(), com.datastax.driver.core.DataType.cint());
			TupleValue tupleValue =
				    tupleType.newValue().setUUID(0, projectMasterData.getProjectId()).setInt(1, projectMasterData.getDate());
			TupleValue tupleValueForDefaultDate =
				    tupleType.newValue().setUUID(0, projectMasterData.getProjectId()).setInt(1, CommonConstants.DEFAULT_DATE);
			tupleValues.add(tupleValue);
			tupleValues.add(tupleValueForDefaultDate);
		}
		
		List<ProjectMasterData> masterDataList = repository.findProjectMasterDataByProjectIdAndDateTuple(superAppId, appId, tupleValues);
		for(ProjectMasterData masterData : masterDataList) {
			if(keyToTargetValueMap.get(masterData.getProjectId()) == null)
				keyToTargetValueMap.put(masterData.getProjectId(), new HashMap<>());
			keyToTargetValueMap.get(masterData.getProjectId()).put(masterData.getKey(), masterData);
		}
		return keyToTargetValueMap;
	}
	
	public void deleteAllRecords(UUID superAppId, UUID appId) {
		repository.deleteAllRecords(superAppId, appId);
	}
	
	public List<UUID> getAllProjectIdsForApp(UUID superAppId, UUID appId, ProjectStates state) {
		List<UUID> ids = new ArrayList<UUID>();
		List<ProjectMasterData> projects = repository.findAllProjectIdsForApp(superAppId, appId);
		if(projects == null || projects.isEmpty() ) {
			return ids;
		} 
		List<ProjectMasterData> projectsStatusData;
		if(!state.getValue().equals(ProjectStates.ALL.getValue())) {
			projectsStatusData = projects.stream().filter(project -> project.getKey().equals(MasterDataKeyNames.STATE_KEY) && !project.getKey().equals(state.getValue())).collect(Collectors.toList());
		} else {
			projectsStatusData = projects.stream().filter(project -> project.getKey().equals(MasterDataKeyNames.STATE_KEY)).collect(Collectors.toList());
		}
		ids = projectsStatusData.stream().map(project -> project.getProjectId()).collect(Collectors.toList());
		return ids;
	}
	
	private List<ProjectMasterData> filterProjectMasterdataForGivenState(List<ProjectMasterData> masterDataList, ProjectStates state) {
		List<ProjectMasterData> data = new ArrayList<ProjectMasterData>();
		if(state == ProjectStates.ALL)  {
			return masterDataList; 
			}
		Map<UUID, List<ProjectMasterData>> projectIdToMasterDataMap = masterDataList.stream().collect(Collectors.groupingBy(ProjectMasterData::getProjectId));
		for(UUID projectId : projectIdToMasterDataMap.keySet()) {
			ProjectMasterData projectsStatus = projectIdToMasterDataMap.get(projectId).stream().filter(projectData -> projectData.getKey().equals(MasterDataKeyNames.STATE_KEY)).collect(Collectors.toList()).stream().findFirst().orElse(null);
			if(projectsStatus != null && state.getValue().equals(projectsStatus.getValue())) {
				data.addAll(projectIdToMasterDataMap.get(projectId));			}
		
		}
		return data;
	}
	private List<UUID> filterProjectIds(UUID superAppId, UUID appId, List<UUID> projectIds, List<ProjectStates> stateList) {
		if(stateList.size() == 1 && stateList.get(0).equals(ProjectStates.ALL)) {
			return projectIds;
		}
		List<UUID> ids = new ArrayList<UUID>();
		Map<UUID, ProjectMasterData> projectIdToMasterDataMap = findDataForProjectsState(superAppId, appId, projectIds);
		for(UUID projectId : projectIdToMasterDataMap.keySet()) {
			ProjectStates state = ProjectStates.getProjectStateNameByValue(projectIdToMasterDataMap.get(projectId).getValue()); 
			if(stateList.contains(state)) {
				ids.add(projectId);
			}
		}
		return ids;
	}
	private List<UUID> filterProjectIds(UUID superAppId, UUID appId, List<UUID> projectIds, ProjectStates state) {
		if(state == ProjectStates.ALL)
			return projectIds;
		List<UUID> ids = new ArrayList<UUID>();
		Map<UUID, ProjectMasterData> projectIdToMasterDataMap = findDataForProjectsState(superAppId, appId, projectIds);
		for(UUID projectId : projectIdToMasterDataMap.keySet()) {
			if(state.getValue().equals(projectIdToMasterDataMap.get(projectId).getValue())) {
				ids.add(projectId);
			}
		}
		return ids;
	}
}