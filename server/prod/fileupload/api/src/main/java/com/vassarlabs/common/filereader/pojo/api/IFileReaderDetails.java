package com.vassarlabs.common.filereader.pojo.api;

import java.util.Map;

import com.vassarlabs.common.fileupload.pojo.api.IFileDetails;

/**
 * Represents file reader details
 * @author vaibhav
 *
 */
public interface IFileReaderDetails extends IFileDetails {

	public Integer getBatchSize();
	public void setBatchSize(Integer batchSize);
	
	public Map<String, String> getColumnNameMapping();
	public void setColumnNameMapping(Map<String, String> columnNameMapping);
	
}