package com.vassarlabs.proj.uniapp.utils;

import com.vassarlabs.proj.uniapp.constants.CommonConstants;

public class DataTypeFormatter {
	public static String getFormatter(String dataType, String pattern) {
		String format = "";
		if(dataType.equalsIgnoreCase(CommonConstants.DATATYPE_DATE)) {
			format = (pattern == null || pattern.isEmpty()) ? CommonConstants.DEFAULT_DATE_FORMAT : pattern;
		} else if(dataType.equalsIgnoreCase(CommonConstants.DATATYPE_DOUBLE)) {
			format = (pattern == null || pattern.isEmpty()) ? CommonConstants.DEFAULT_DOUBLE_FORMAT : pattern;
		} else if(dataType.equalsIgnoreCase(CommonConstants.DATATYPE_TIME)) {
			format = (pattern == null || pattern.isEmpty()) ? CommonConstants.DEFAULT_TIME_FORMAT : pattern;
		} else if(dataType.equalsIgnoreCase(CommonConstants.DATATYPE_TIMESTAMP)) {
			format = (pattern == null || pattern.isEmpty()) ? CommonConstants.DEFAULT_TIMESTAMP_FORMAT : pattern;
		} else {
			format = pattern;
		}
		return format;
		
	} 
	
}
