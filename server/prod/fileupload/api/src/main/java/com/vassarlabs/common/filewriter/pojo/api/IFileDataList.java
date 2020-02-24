package com.vassarlabs.common.filewriter.pojo.api;

import java.util.List;

/**
 * Data List
 * @author vaibhav
 *
 */
public interface IFileDataList<E> {

	public List<E> getDataList();
	public void setDataList(List<E> dataList);
	
}
