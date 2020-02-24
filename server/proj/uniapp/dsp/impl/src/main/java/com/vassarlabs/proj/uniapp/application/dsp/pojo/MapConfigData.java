package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "map_config_data")
@Data
public class MapConfigData {

	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	 UUID superAppId;
	 
	 @PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	 UUID appId;
	 
	 @PrimaryKeyColumn(name = "version", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	 int versionNumber;
	 
	 @Column(value = "markers_info")
	 String markersInfo;
	 
	 @Column(value = "config_data")
	 String configData;
	 
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
		 ApplicationMetaData other = (ApplicationMetaData) obj;
		 if(superAppId != other.superAppId) 
			 return false;
		 if (appId != other.appId)
			 return false;
		 if(versionNumber != other.versionNumber) 
			 return false;
		 return true;
	 }

	 @Override
	 public int hashCode() {
		 final int prime = 31;
		 int result = 1;
		 result = (prime * result + superAppId.hashCode());
		 result =  (prime * result + appId.hashCode());
		 result =  (prime * result + versionNumber);
		 return result;
	 }
}
