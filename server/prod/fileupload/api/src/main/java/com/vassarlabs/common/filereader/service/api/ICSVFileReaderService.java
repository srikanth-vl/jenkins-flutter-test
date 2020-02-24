package com.vassarlabs.common.filereader.service.api;

import java.io.IOException;

import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;

/**
 * Reads different types of files and send its results in various forms
 * 
 * @author vaibhav
 *
 */
public interface ICSVFileReaderService {
	
	/**
	 * Reads a CSV file and returns <List> of  models
	 * @param fileUploadDetails
	 * @param fileUploadResult 
	 * @return 
	 * @throws IOException
	 */
	public <E> void readCSVFile(IFileUploadDetails fileUploadDetails, IFileUploadResult<E> fileUploadResult) throws IOException;

}