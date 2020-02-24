package com.vassarlabs.common.filewriter.pojo.impl;

import com.vassarlabs.common.fileupload.pojo.impl.FileDetails;
import com.vassarlabs.common.filewriter.pojo.api.IFileWriterDetails;

public class FileWriterDetails extends FileDetails implements IFileWriterDetails {

	protected Boolean isAppend;
	protected String errorFileName;
	
	@Override
	public Boolean getIsAppend() {
		return this.isAppend;
	}

	@Override
	public void setIsAppend(Boolean isAppend) {
		this.isAppend = isAppend;
	}

	@Override
	public String getErrorFileName() {
		return errorFileName;
	}

	@Override
	public void setErrorFileName(String fileName) {
		this.errorFileName = fileName;
	}

	@Override
	public String toString() {
		return "FileWriterDetails [isAppend=" + isAppend + ", errorFileName=" + errorFileName + "]";
	}
}