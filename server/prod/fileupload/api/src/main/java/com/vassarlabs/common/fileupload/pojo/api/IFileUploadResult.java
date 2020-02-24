package com.vassarlabs.common.fileupload.pojo.api;

import java.util.List;
import java.util.Map;

import com.vassarlabs.common.utils.err.IErrorObject;

public interface IFileUploadResult<E> {

	int getNoOfRecords();
	void setNoOfRecords(int noOfRecords);

	int getNoOfSuccessfulRecords();
	void setNoOfSuccessfulRecords(int noOfSuccessfulRecords);

	int getNoOfErrorRecords();
	void setNoOfErrorRecords(int noOfErrorRecords);

	IFileDetails getFileDetails();
	void setFileDetails(IFileDetails fileDetails);

	int getUploadStatus();
	void setUploadStatus(int uploadStatus);
	
	int getNoOfInsertedRecords();
	void setNoOfInsertedRecords(int noOfInsertedRecords);

	int getNoOfUpdatedRecords();
	void setNoOfUpdatedRecords(int noOfUpdatedRecords);
	
	Map<E, List<IErrorObject>> getDataToErrorListMap();
	void setDataToErrorListMap(Map<E, List<IErrorObject>> dataToErrorListMap);
}
