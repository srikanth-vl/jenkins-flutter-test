package com.vassarlabs.common.filereader.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.filereader.service.api.ICSVFileReaderService;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;
import com.vassarlabs.common.fileupload.service.api.IFileUploadService;
import com.vassarlabs.common.fileupload.utils.FileUploadConstants;

@Component("FileUploadService")
public class FileUploadService <E>
	implements IFileUploadService<E> {
	
	@Autowired
	protected ICSVFileReaderService fileReaderService;

	@Override
	public void uploadFile(IFileUploadDetails fileUploadDetails, IFileUploadResult<E> fileUploadResult) throws FileNotFoundException, IOException  {
		fileReaderService.readCSVFile(fileUploadDetails, fileUploadResult);
	}
	
	protected IFileUploadResult<E> appendFileUploadResult(IFileUploadResult<E> fileUploadResult, IFileUploadResult<E> resultToAppend) {
	
		if (fileUploadResult == null) {
			fileUploadResult = resultToAppend;
		} else{
			fileUploadResult.setNoOfRecords(fileUploadResult.getNoOfRecords() + resultToAppend.getNoOfRecords());
			fileUploadResult.setNoOfInsertedRecords(fileUploadResult.getNoOfInsertedRecords() + resultToAppend.getNoOfInsertedRecords());
			fileUploadResult.setNoOfErrorRecords(fileUploadResult.getNoOfErrorRecords() + resultToAppend.getNoOfErrorRecords());
			fileUploadResult.setNoOfSuccessfulRecords(fileUploadResult.getNoOfSuccessfulRecords() + resultToAppend.getNoOfSuccessfulRecords());
			fileUploadResult.setNoOfUpdatedRecords(fileUploadResult.getNoOfUpdatedRecords() + resultToAppend.getNoOfUpdatedRecords());
			fileUploadResult.setUploadStatus((fileUploadResult.getUploadStatus() == FileUploadConstants.FILE_UPLOAD_FAILURE || resultToAppend.getUploadStatus() == FileUploadConstants.FILE_UPLOAD_FAILURE) ? FileUploadConstants.FILE_UPLOAD_FAILURE : FileUploadConstants.FILE_UPLOAD_SUCCESS);
		}
			
		return fileUploadResult;
	}
}