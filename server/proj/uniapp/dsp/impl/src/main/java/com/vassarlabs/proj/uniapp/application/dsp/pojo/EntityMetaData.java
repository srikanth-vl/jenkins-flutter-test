package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Table(value = "entity_meta_data")
@Data
public class EntityMetaData {
	@JsonProperty("super_app_id")
	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@JsonProperty("app_id")
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID applicationId;
	@JsonProperty("project_id")
	@PrimaryKeyColumn(name = "project_id", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
	UUID projectId;
	@JsonProperty("user_id")
	@PrimaryKeyColumn(name = "user_id", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	String userId;
	@JsonProperty("parent_entity")
	@PrimaryKeyColumn(name = "parent_entity", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
	String parentEntity;
	
	@JsonProperty("entity_name")
	@PrimaryKeyColumn(name = "entity_name", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
	String entityName;
	
	@JsonProperty("insert_ts")
	@PrimaryKeyColumn(name = "insert_ts", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
	long insertTs;
	
	@JsonProperty("elements")
	@Column(value = "elements")
	String elements;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityMetaData other = (EntityMetaData) obj;
		if (!superAppId.equals(other.superAppId))
			return false;
		if (!applicationId.equals(applicationId))
			return false;
		if (!projectId.equals(other.projectId))
			return false;
		if (!parentEntity.equals(other.parentEntity))
			return false;
		if (entityName != other.entityName)
			return false;
		if (userId != other.userId)
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
		result = prime * result + userId.hashCode();
		result = prime * result + insertTs;
		result = prime * result + parentEntity.hashCode();
		result = prime * result + entityName.hashCode();

		return (int) result;
	}
}
