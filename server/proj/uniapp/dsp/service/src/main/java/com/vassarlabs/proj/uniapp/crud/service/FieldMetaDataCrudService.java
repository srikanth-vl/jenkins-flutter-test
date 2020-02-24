package com.vassarlabs.proj.uniapp.crud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import com.datastax.driver.core.TupleType;
import com.datastax.driver.core.TupleValue;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.dsp.repository.FieldMetaDataRepository;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;

@Component
public class FieldMetaDataCrudService {
	
	@Autowired FieldMetaDataRepository repository;
	
	public void insertFieldMetaData(FieldMetaData data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.save(data);
	}
	
	public void deleteFieldMetaData(FieldMetaData data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.delete(data);
	}
	
	public void deleteFieldMetaDataList(List<FieldMetaData> metaDataList) {
		repository.deleteAll(metaDataList);
	}
	
	public void insertFieldMetaData(List<FieldMetaData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.saveAll(data);
	}
	
	public List<FieldMetaData> findFieldMetaDataByPartitionKey(UUID superAppId, UUID appId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		return repository.findByPartitionKey(superAppId, appId);
	}
	
	public FieldMetaData findFieldMetaDataByPrimaryKey(UUID superAppId, UUID appId, UUID projectId, int formType, int metaDataVersion, int keyType, String key) 
			throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, 
			CassandraQuerySyntaxException, CassandraTypeMismatchException {
		return repository.findByPrimaryKey(superAppId, appId, projectId, formType, metaDataVersion, keyType, key);
	}
	
	/**
	 * Get all the fields corresponding to a project
	 * @param superAppId
	 * @param appId
	 * @param metaDataInstanceIds
	 * @return
	 */
	public Map<UUID, List<FieldMetaData>> getProjectIdToFieldsData(UUID superAppId, UUID appId, List<String> metaDataInstanceIds) {
		
		List<TupleValue> projectTuples = new ArrayList<>();
		for (String metaDataInstanceId : metaDataInstanceIds) {
			String[] args = metaDataInstanceId.split("##");
			if(args.length == 5) {
				UUID projectId = UUIDUtils.toUUID(args[2]);
				int formType = Integer.parseInt(args[3]);
				int metaVersion = Integer.parseInt(args[4]);
				TupleType tupleType = TupleType.of(com.datastax.driver.core.ProtocolVersion.NEWEST_SUPPORTED, 
						com.datastax.driver.core.CodecRegistry.DEFAULT_INSTANCE,
						com.datastax.driver.core.DataType.uuid(), com.datastax.driver.core.DataType.cint(), com.datastax.driver.core.DataType.cint());
				TupleValue tupleValue =
					    tupleType.newValue().setUUID(0, projectId).setInt(1, formType).setInt(2, metaVersion);
				projectTuples.add(tupleValue);
			}
		}
		
		Map<UUID, List<FieldMetaData>> projectIdToFieldsDataMap = new HashMap<>();
		List<FieldMetaData> appFieldsData = repository.findAllFieldsOfProjects(superAppId, appId, projectTuples);
		for(FieldMetaData metaData : appFieldsData) {
			if(!projectIdToFieldsDataMap.containsKey(metaData.getProjectId())) {
				projectIdToFieldsDataMap.put(metaData.getProjectId(), new ArrayList<>());
			}
			projectIdToFieldsDataMap.get(metaData.getProjectId()).add(metaData);
		}
		return projectIdToFieldsDataMap;
	}
	
	/**
	 * Get all the fields corresponding to a project
	 * @param superAppId
	 * @param appId
	 * @param metaDataInstanceIds
	 * @return
	 */
	public Map<UUID, List<FieldMetaData>> getProjectIdAndDefaultIdToFieldsData(UUID superAppId, UUID appId, List<String> metaDataInstanceIds) {
		
		List<TupleValue> projectTuples = new ArrayList<>();
		if(superAppId == null || appId == null || metaDataInstanceIds.isEmpty()) {
			return null;
		}
		TupleType tupleType = TupleType.of(com.datastax.driver.core.ProtocolVersion.NEWEST_SUPPORTED, 
				com.datastax.driver.core.CodecRegistry.DEFAULT_INSTANCE,
				com.datastax.driver.core.DataType.uuid(), com.datastax.driver.core.DataType.cint());
		Set<String> distinctMetaDataInstanceIds = new HashSet<>(metaDataInstanceIds);
		for (String metaDataInstanceId : metaDataInstanceIds) {
			String[] args = metaDataInstanceId.split("##");
			if(args.length == 5) {
				UUID projectId = UUIDUtils.toUUID(args[2]);
				int formType = Integer.parseInt(args[3]);
				TupleValue tupleValue =
					    tupleType.newValue().setUUID(0, projectId).setInt(1, formType);
				projectTuples.add(tupleValue);
			}
		}
		
		for(FormTypes formType : FormTypes.values())
			projectTuples.add(tupleType.newValue().setUUID(0, UUIDUtils.getDefaultUUID()).setInt(1, formType.getValue()));
		
		Map<UUID, List<FieldMetaData>> projectIdToFieldsDataMap = new HashMap<>();
		List<FieldMetaData> appFieldsData = repository.findAllFieldsOfProjectsWithoutVersion(superAppId, appId, projectTuples);
		distinctMetaDataInstanceIds.addAll(appFieldsData.stream().filter(e -> e.getProjectId() == UUIDUtils.getDefaultUUID()).map(fieldMetaData -> fieldMetaData.getMetadataInstanceId()).collect(Collectors.toList()));
		
		for(FieldMetaData metaData : appFieldsData) {
			if(!distinctMetaDataInstanceIds.contains(metaData.getMetadataInstanceId()))
				continue;
			if(!projectIdToFieldsDataMap.containsKey(metaData.getProjectId())) {
				projectIdToFieldsDataMap.put(metaData.getProjectId(), new ArrayList<>());
			}
			projectIdToFieldsDataMap.get(metaData.getProjectId()).add(metaData);
		}
		
		return projectIdToFieldsDataMap;
	
	}
	
	public FieldMetaData findLatestMetaVersion(UUID superAppId, UUID appId, UUID projectId, int formType) {
		return repository.findLatestMetaVersion(superAppId, appId, projectId, formType);
	}
	
	public List<FieldMetaData> getFieldData(UUID superAppId, UUID appId, String metaDataInstanceId) {
		
		String[] args = metaDataInstanceId.split("##");
		if(args.length == 5) {
			UUID projectId = UUIDUtils.toUUID(args[2]);
			int formType = Integer.parseInt(args[3]);
			int metaVersion = Integer.parseInt(args[4]);
			return repository.findFieldMetaData(superAppId, appId, projectId, formType, metaVersion);
		} else {
			return null;
		}
	}

	public List<FieldMetaData> findLatestFieldMetaDataForProjects(UUID superAppId, UUID appId, List<UUID> projectIdList, int formType) {
		List<FieldMetaData> fieldMetaDataList =  repository.findLatestFieldMetaDataForProjects(superAppId, appId, projectIdList, formType);
		return fieldMetaDataList;
	}
	
	public Map<UUID, List<FieldMetaData>> findLatestFieldMetaForKeyTypeForProjects(UUID superAppId, UUID appId, List<UUID> projectIdList,int formType, int keyType) {
		List<FieldMetaData> fieldMetaDataList =  repository.findLatestFieldMetaDataForProjects(superAppId, appId, projectIdList, formType);
		Map<UUID, List<FieldMetaData>> projectIdToKeyTypeData = fieldMetaDataList.stream().filter(p -> p.getKeyType() == keyType).collect(Collectors.groupingBy(FieldMetaData::getProjectId));
		return projectIdToKeyTypeData;
	}

	public Map<String, List<FieldMetaData>> findMdInstanceIdTofieldMetaDataListMapForApp(UUID superAppId, UUID applicationId) {
		Map<String, List<FieldMetaData>> latestMetaDataMap = new HashMap<>();
		List<FieldMetaData> fieldMetaDataList = findFieldMetaDataByPartitionKey(superAppId, applicationId);
		latestMetaDataMap = fieldMetaDataList.stream().collect(Collectors.groupingBy(FieldMetaData :: getMetadataInstanceId));
		return latestMetaDataMap;
	}

	public Map<String, Map<String,String>> findMdInstanceIdTofieldMetaDataKeyToDataTypeMap(UUID superAppId, UUID applicationId) {
		Map<String, List<FieldMetaData>> latestMetaDataMap = new HashMap<>();
		List<FieldMetaData> fieldMetaDataList = findFieldMetaDataByPartitionKey(superAppId, applicationId);
		latestMetaDataMap = fieldMetaDataList.stream().collect(Collectors.groupingBy(FieldMetaData :: getMetadataInstanceId));
		Map<String, Map<String, String>> mdInstanceIdToKeyToValuesMap = new HashMap<>();
		for(String mdInstanceId : latestMetaDataMap.keySet()) {
			List<FieldMetaData> fields = latestMetaDataMap.get(mdInstanceId);
			Map<String, String> keyToValuesMap = new HashMap<>();
			keyToValuesMap = fields.stream().collect(Collectors.toMap(FieldMetaData :: getKey, FieldMetaData :: getDataType, (oldValue, newValue) -> oldValue));
			mdInstanceIdToKeyToValuesMap.put(mdInstanceId, keyToValuesMap);
		}
		return mdInstanceIdToKeyToValuesMap;
	}
}
