package com.vassarlabs.common.filewriter.pojo.api;

import com.vassarlabs.common.fileupload.pojo.api.IFileDetails;

/**
 * Represents file writer details
 * @author vaibhav
 *
 */
public interface IFileWriterDetails extends IFileDetails {

	public Boolean getIsAppend();
	public void setIsAppend(Boolean isAppend);
	
	public String getErrorFileName();
	public void setErrorFileName(String fileName);	
}