package com.vassarlabs.common.fileupload.service.api;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.vassarlabs.common.fileupload.pojo.api.IFileDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;

/**
 * Service for File Upload, will be used when overriding with a child class.
 * 
 * <p>
 * {@link IFileDetails}
 * {@link Class<E>}
 * </p>
 * 
 * @author vaibhav
 *
 */
public interface IFileUploadService<E> {

	void uploadFile(IFileUploadDetails fileUploadDetails, IFileUploadResult<E> fileUploadResult)
			throws FileNotFoundException, IOException;

}