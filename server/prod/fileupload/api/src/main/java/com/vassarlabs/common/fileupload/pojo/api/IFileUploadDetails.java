package com.vassarlabs.common.fileupload.pojo.api;

import java.util.Properties;

public interface IFileUploadDetails {
	
	public String getFileName();
	public void setFileName(String fileName);
	
	public String getFileFullPath();
	public void setFileFullPath(String fullPath);
	
	public String getDelimiter();
	public void setDelimiter(String delimiter);
	
	public char getQuotedChar();
	public void setQuotedChar(char quotedChar);
	
	public Long getFileUploadTs();
	public void setFileUploadTs(Long fileUploadTs);

	public Integer getBatchSize();
	public void setBatchSize(Integer batchSize);
	
	public Properties getProperties();
	public void setProperties(Properties properties);
	
	public String getClassName();
	public void setClassName(String className);
	
	public String getErrorFileName();
	public void setErrorFileName(String errorFileName);
	
}
