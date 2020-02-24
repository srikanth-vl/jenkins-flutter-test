package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;
import lombok.ToString;

@Table(value = "project_master_data")
@Data
@ToString
public class ProjectMasterData {
	
	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID applicationId;
	
	@PrimaryKeyColumn(name = "project_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	UUID projectId;
	
	@PrimaryKeyColumn(name = "date", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	int date;
	
	@PrimaryKeyColumn(name = "key", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
	String key;
	
	@Column(value = "value")
	String value;
	
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
		ProjectMasterData other = (ProjectMasterData) obj;
		if (superAppId != other.superAppId)
			return false;
		if (applicationId != other.applicationId)
			return false;
		if (projectId != other.projectId)
			return false;
		if (date != other.date)
			return false;
		if (key != other.key)
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + superAppId.hashCode();
		result = prime * result + applicationId.hashCode();
		result = prime * result + projectId.hashCode();		
		result = prime * result + date;
		result = prime * result + key.hashCode();
		return (int) result;
	}
}
