package com.vassarlabs.common.fileupload.event.impl;

import java.util.List;

import org.springframework.core.ResolvableType;

import com.vassarlabs.common.fileupload.event.api.IFileUploadEvent;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;

public class FileUploadEvent<E> implements IFileUploadEvent<E>{

	protected Class<?> classType;
	protected List<E> dataList;
	protected IFileUploadDetails fileUploadDetails;
	protected IFileUploadResult<E> fileUploadResult;
    
	public FileUploadEvent(Class<?> classType, IFileUploadDetails fileUploadDetails, List<E> dataList, IFileUploadResult<E> fileUploadResult) {
		super();
		this.classType = classType;
		this.dataList = dataList;
		this.fileUploadDetails = fileUploadDetails;
		this.fileUploadResult = fileUploadResult;
	}

	@Override
    public Class<?> getClassType() {
		return classType;
	}

	@Override
    public void setClassType(Class<?> classType) {
		this.classType = classType;
	}

	@Override
    public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(classType));
    }
    
	@Override
	public IFileUploadDetails getFileUploadDetails() {
		return fileUploadDetails;
	}

	@Override
	public void setFileUploadDetails(IFileUploadDetails fileUploadDetails) {
		this.fileUploadDetails = fileUploadDetails;
	}

	@Override
	public List<E> getDataList() {
		return dataList;
	}

	@Override
	public void setDataList(List<E> dataList) {
		this.dataList = dataList;
	}

	@Override
	public IFileUploadResult<E> getFileUploadResult() {
		return fileUploadResult;
	}

	@Override
	public void setFileUploadResult(IFileUploadResult<E> fileUploadResult) {
		this.fileUploadResult = fileUploadResult;
	}

	@Override
	public String toString() {
		return "FileUploadEvent [classType=" + classType + ", dataList=" + dataList + ", fileUploadDetails="
				+ fileUploadDetails + "]";
	}
}
