package com.vassarlabs.common.filewriter.pojo.impl;

import java.util.List;

import com.vassarlabs.common.filewriter.pojo.api.IFileDataList;

public class FileDataList<E> implements IFileDataList<E> {

	private List<E> dataList;
	
	@Override
	public List<E> getDataList() {
		return this.dataList;
	}

	@Override
	public void setDataList(List<E> dataList) {
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "FileDataList [dataList=" + dataList + "]";
	}

}
