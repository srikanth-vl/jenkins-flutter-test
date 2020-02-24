package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class OffLineMapFile {
	
	@JsonProperty("file_name")
	public String fileName;
	
	@JsonProperty("file_url")
	public String fileUrl;
	
	@JsonProperty("file_storage_path")
	public String fileStoragePath;
	
	@JsonProperty("file_size")
	public String fileSize;
	
	@JsonProperty("file_additional_info")
	public Map<String, String> fileAdditionalInfo;
}
