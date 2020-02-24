package com.vassarlabs.common.fileupload.pojo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vassarlabs.common.fileupload.pojo.api.IFileDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;
import com.vassarlabs.common.utils.err.IErrorObject;

public class FileUploadResult<E> implements IFileUploadResult <E> {

	protected int noOfRecords;
	protected int noOfSuccessfulRecords;
	protected int noOfErrorRecords;
	protected int uploadStatus;
	protected int noOfInsertedRecords;
	protected int noOfUpdatedRecords;
	Map<E, List<IErrorObject>> dataToErrorListMap;
	protected IFileDetails fileDetails;
	
	public FileUploadResult() {
		super();
		dataToErrorListMap = new HashMap<>();
	}
	
	@Override
	public int getNoOfInsertedRecords() {
		return noOfInsertedRecords;
	}
	@Override
	public void setNoOfInsertedRecords(int noOfInsertedRecords) {
		this.noOfInsertedRecords = noOfInsertedRecords;
	}
	@Override
	public int getNoOfUpdatedRecords() {
		return noOfUpdatedRecords;
	}
	@Override
	public void setNoOfUpdatedRecords(int noOfUpdatedRecords) {
		this.noOfUpdatedRecords = noOfUpdatedRecords;
	}
	@Override
	public int getNoOfRecords() {
		return noOfRecords;
	}
	@Override
	public void setNoOfRecords(int noOfRecords) {
		this.noOfRecords = noOfRecords;
	}
	
	@Override
	public int getNoOfSuccessfulRecords() {
		return noOfSuccessfulRecords;
	}
	@Override
	public void setNoOfSuccessfulRecords(int noOfSuccessfulRecords) {
		this.noOfSuccessfulRecords = noOfSuccessfulRecords;
	}
	
	@Override
	public int getNoOfErrorRecords() {
		return noOfErrorRecords;
	}
	@Override
	public void setNoOfErrorRecords(int noOfErrorRecords) {
		this.noOfErrorRecords = noOfErrorRecords;
	}

	@Override
	public Map<E, List<IErrorObject>> getDataToErrorListMap() {
		return dataToErrorListMap;
	}

	@Override
	public void setDataToErrorListMap(Map<E, List<IErrorObject>> dataToErrorListMap) {
		this.dataToErrorListMap = dataToErrorListMap;
	}

	@Override
	public IFileDetails getFileDetails() {
		return fileDetails;
	}
	@Override
	public void setFileDetails(IFileDetails fileDetails) {
		this.fileDetails = fileDetails;
	}
	
	@Override
	public int getUploadStatus() {
		return uploadStatus;
	}
	@Override
	public void setUploadStatus(int uploadStatus) {
		this.uploadStatus = uploadStatus;
	}
	
}
