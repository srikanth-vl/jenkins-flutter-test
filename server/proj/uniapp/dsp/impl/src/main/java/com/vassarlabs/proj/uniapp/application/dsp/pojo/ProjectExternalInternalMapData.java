package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "project_external_to_internal_mapping")
@Data
public class ProjectExternalInternalMapData {

	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID appId;

	@PrimaryKeyColumn(name = "proj_ext_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	String projectExternalId;
	
	@PrimaryKeyColumn(name = "project_id", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	UUID projectId;

	@Column(value = "insert_ts")
	long insertTs;

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		ProjectExternalInternalMapData other = (ProjectExternalInternalMapData) obj;
		
		if (!superAppId.equals(other.superAppId))
			return false;
		
		if (!appId.equals(other.appId))
			return false;
		
		if (!projectExternalId.equals(other.projectExternalId))
			return false;
		
		if (!projectId.equals(other.projectId))
			return false;
		
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + superAppId.hashCode();
		result = prime * result + appId.hashCode();
		result = prime * result + projectExternalId.hashCode();
		result = prime * result + projectId.hashCode();
		return result;
	}
	
}