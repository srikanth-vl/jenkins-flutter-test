package com.vassarlabs.common.fileupload.pojo.impl;

import java.util.Properties;

import com.vassarlabs.common.fileupload.pojo.api.IFileDetails;

public class FileDetails implements IFileDetails{

	protected String fileName;
	protected String fullFilePath;
	protected char quotedChar;
	protected String delimiter;
	protected Properties properties;
	
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
	public String toString() {
		return "FileDetails [fileName=" + fileName + ", fullFilePath=" + fullFilePath + ", quotedChar=" + quotedChar
				+ ", delimiter=" + delimiter + ", properties=" + properties + "]";
	}

}
