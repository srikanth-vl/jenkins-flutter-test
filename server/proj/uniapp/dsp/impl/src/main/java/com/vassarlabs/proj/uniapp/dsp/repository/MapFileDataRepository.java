package com.vassarlabs.proj.uniapp.dsp.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapFileData;

@Repository
public interface MapFileDataRepository 
	extends CrudRepository<MapFileData, String>{
	
	@Query("SELECT * from map_files"
			+ " WHERE name = ?0")
	MapFileData findByPartitionKey(String mapFileName);

}
