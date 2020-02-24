package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "superapp_metadata")
@Data
public class SuperApplicationData {
	
	 @PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	 UUID superAppId;
	 
	 @PrimaryKeyColumn(name = "version", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	 int versionNumber;
	 
	 @Column(value = "name")
	 String name;
	 
	 @Column(value = "config_file")
	 String configFile;
	 
	 @Column(value = "aws_bucket_name")
	 String awsProperties;
	 
	 @Column(value = "package_name")
	 String packageName;
	 	 
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
			SuperApplicationData other = (SuperApplicationData) obj;
			if (superAppId != other.superAppId)
				return false;
			if (versionNumber != other.versionNumber)
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result =  prime * result + superAppId.hashCode();
			result = prime * result + versionNumber;
			return result;
		}
}
