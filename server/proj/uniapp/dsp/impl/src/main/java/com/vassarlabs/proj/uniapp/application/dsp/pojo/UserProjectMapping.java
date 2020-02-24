package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(value = "user_project_mapping")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserProjectMapping {
	
	 @PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	 UUID superAppId;
	 
	 @PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	 UUID appId;
	 
	 @PrimaryKeyColumn(name = "user_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	 String userId;
	 
	 @PrimaryKeyColumn(name = "user_type", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	 int userType;
	 
	 @Column(value = "project_list")
	 List<UUID> projectList;
	 
	 @Column(value = "insert_ts")
	 long insertTs;
	 
}
