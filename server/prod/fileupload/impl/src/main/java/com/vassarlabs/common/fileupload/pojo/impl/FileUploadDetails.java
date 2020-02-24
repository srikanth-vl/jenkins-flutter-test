package com.vassarlabs.common.fileupload.pojo.impl;

import java.util.Properties;

import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;

public class FileUploadDetails implements IFileUploadDetails {

	protected String fileName;
	protected String fullFilePath;
	protected char quotedChar;
	protected String delimiter;
	protected Long fileUploadTs;
	protected Integer batchSize;
	protected Properties properties;
	protected String className;
	protected String errorFileName;
	
	@Override
	public String getFileName() {
		return this.fileName;
	}

	@Override
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String getDelimiter() {
		return this.delimiter;
	}

	@Override
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public char getQuotedChar() {
		return this.quotedChar;
	}

	@Override
	public void setQuotedChar(char quotedChar) {
		this.quotedChar = quotedChar;
	}
	
	@Override
	public Long getFileUploadTs() {
		return this.fileUploadTs;
	}

	@Override
	public void setFileUploadTs(Long fileUploadTs) {
		this.fileUploadTs = fileUploadTs;
	}

	@Override
	public Integer getBatchSize() {
		return this.batchSize;
	}

	@Override
	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}
	
	@Override
	public String getFileFullPath() {
		return this.fullFilePath;
	}

	@Override
	public void setFileFullPath(String fullFilePath) {
		this.fullFilePath = fullFilePath;
	}
	
	@Override
	public Properties getProperties() {
		return this.properties;
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public String getErrorFileName() {
		return errorFileName;
	}

	@Override
	public void setErrorFileName(String errorFileName) {
		this.errorFileName = errorFileName;
	}

	@Override
	public String toString() {
		return "FileUploadDetails [fileName=" + fileName + ", fullFilePath=" + fullFilePath + ", quotedChar="
				+ quotedChar + ", delimiter=" + delimiter + ", fileUploadTs=" + fileUploadTs + ", batchSize="
				+ batchSize + ", properties=" + properties + ", className=" + className + "]";
	}
}