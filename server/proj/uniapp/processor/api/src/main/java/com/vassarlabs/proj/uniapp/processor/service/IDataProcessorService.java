package com.vassarlabs.proj.uniapp.processor.service;

import com.vassarlabs.proj.uniapp.processor.pojo.UniappFormDataList;
import com.vassarlabs.proj.uniapp.processor.pojo.UniappMediaData;

public interface IDataProcessorService {
	
	public Boolean processTextData(UniappFormDataList dataList);
	public Boolean processImageData(UniappMediaData mediaValue);
}
