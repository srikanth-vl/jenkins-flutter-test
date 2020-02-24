package com.vassarlabs.common.fileupload.aspect.api;

import org.aspectj.lang.JoinPoint;

import com.vassarlabs.common.filereader.pojo.api.IFileReaderDetails;
import com.vassarlabs.common.utils.err.InsufficientDataException;

/**
 * Aspect for file upload
 * @author vaibhav
 *
 */
public interface IFileUploadAspect {
	
	/**
	 * 
	 * @param joinPoint
	 * @param fileReaderDetails
	 * @throws InsufficientDataException
	 */
	public void verifyUploadDetails(JoinPoint joinPoint, IFileReaderDetails fileReaderDetails) throws Exception;
	
}