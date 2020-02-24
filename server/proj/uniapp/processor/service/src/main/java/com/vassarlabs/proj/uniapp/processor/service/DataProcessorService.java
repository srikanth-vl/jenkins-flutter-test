package com.vassarlabs.proj.uniapp.processor.service;

import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.processor.pojo.UniappFormDataList;
import com.vassarlabs.proj.uniapp.processor.pojo.UniappMediaData;

@Component
public class DataProcessorService 
	implements IDataProcessorService {

	@Override
	public Boolean processTextData(UniappFormDataList dataList) {
		System.out.println("Inside processing of text data from uniapp");
		System.out.println(dataList);
		// TODO: Write project specific code here
		return true;
	}

	@Override
	public Boolean processImageData(UniappMediaData mediaValue) {
		System.out.println("Inside processing of media data from uniapp");
		byte[] mediaContent = mediaValue.getMediaContent();
		System.out.println("Size of media content recieved is- " + mediaContent.length);
		//System.out.println(mediaValue);
		// TODO: Write project specific code here
		return true;
	}
}
