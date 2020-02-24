package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "map_files")
@Data
public class MapFileData {
		 
	 @PrimaryKeyColumn(name = "name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	 String mapFileName;
	 
	 @Column(value = "size")
	 String fileSize;
	 
	 @Column(value = "url")
	 String fileUrl;
	 
	 @Column(value = "uploaded")
	 Boolean uploaded;
	 	 
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
			MapFileData other = (MapFileData) obj;
			if (!mapFileName.equals(other.mapFileName))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result =  prime * result + mapFileName.hashCode();
			return result;
		}
}
