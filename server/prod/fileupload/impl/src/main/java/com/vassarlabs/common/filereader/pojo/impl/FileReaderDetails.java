package com.vassarlabs.common.filereader.pojo.impl;

import java.util.Map;

import com.vassarlabs.common.filereader.pojo.api.IFileReaderDetails;
import com.vassarlabs.common.fileupload.pojo.impl.FileDetails;

public class FileReaderDetails extends FileDetails implements IFileReaderDetails {
	
	protected Integer batchSize;
	protected Map<String, String> columnNameMapping;
	
	@Override
	public Integer getBatchSize() {
		return this.batchSize;
	}

	@Override
	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}
	
	@Override
	public Map<String, String> getColumnNameMapping() {
		return this.columnNameMapping;
	}

	@Override
	public void setColumnNameMapping(Map<String, String> columnNameMapping) {
		this.columnNameMapping = columnNameMapping;
	}

	@Override
	public String toString() {
		return "FileReaderDetails [batchSize=" + batchSize + ", columnNameMapping=" + columnNameMapping + ", fileName="
				+ fileName + ", fullFilePath=" + fullFilePath + ", quotedChar=" + quotedChar + ", delimiter="
				+ delimiter + ", properties=" + properties + "]";
	}
	
}
