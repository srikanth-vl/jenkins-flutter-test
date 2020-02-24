package com.vassarlabs.common.fileupload.pojo.api;

import java.util.Properties;

/**
 * Represents File Details
 * 
 * @author vaibhav
 *
 */
public interface IFileDetails {

	public String getFileName();
	public void setFileName(String fileName);
	
	public String getFileFullPath();
	public void setFileFullPath(String fullPath);
	
	public String getDelimiter();
	public void setDelimiter(String delimiter);
	
	public char getQuotedChar();
	public void setQuotedChar(char quotedChar);

	public Properties getProperties();
	public void setProperties(Properties properties);
	
}
