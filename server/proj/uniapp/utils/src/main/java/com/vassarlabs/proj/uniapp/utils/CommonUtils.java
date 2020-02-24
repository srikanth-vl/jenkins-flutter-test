package com.vassarlabs.proj.uniapp.utils;

import java.util.ArrayList;
import java.util.List;

import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.utils.api.pojo.MediaEntity;
public class CommonUtils {
	
	public static List<MediaEntity> getValuesFromMediaTextData(String value) {	
	   String [] geovalues = value.split(",");
	   ArrayList<MediaEntity> finalMediaEntity = new ArrayList<>(); 
		for(int i=0;i<geovalues.length;i++) {
			MediaEntity mediaEntity = null;
			String [] mediaValues = geovalues[i].split(CommonConstants.DELIMITER);
			if(mediaValues.length == 3) {
				mediaEntity = new MediaEntity(UUIDUtils.toUUID(mediaValues[0]),
						Double.parseDouble(mediaValues[1]), Double.parseDouble(mediaValues[2]));
			}else {
				mediaEntity = new MediaEntity();
				mediaEntity.setMediaUUID(UUIDUtils.toUUID(mediaValues[0]));
			}
		finalMediaEntity.add(mediaEntity);
		}

		return finalMediaEntity;	
	}


}