package com.vassarlabs.common.fileupload.event.api;

import java.util.List;

import org.springframework.core.ResolvableTypeProvider;

import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;

/**
 * Event publisher for file upload
 * @author vaibhav
 *
 * @param <E>
 */
public interface IFileUploadEvent<E> extends ResolvableTypeProvider {

	public Class<?> getClassType();
	public void setClassType(Class<?> source);

	public IFileUploadDetails getFileUploadDetails();
	public void setFileUploadDetails(IFileUploadDetails fileUploadDetails);

	public List<E> getDataList();
	public void setDataList(List<E> dataList);
	
	IFileUploadResult<E> getFileUploadResult();
	void setFileUploadResult(IFileUploadResult<E> fileUploadResult);

}
