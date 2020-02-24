package com.vassarlabs.proj.uniapp.enums;

import java.util.HashMap;
import java.util.Map;

public enum KeyTypes {
	
	DEFAULT_KEY(0),
	MASTER_DATA_KEY(1),
	APP_DATA_KEY(2);
	
	
	private static final Map<Integer, KeyTypes> keyTypeToNameMap = new HashMap<Integer, KeyTypes>();
	private final Integer value;
	
	static {
		for (KeyTypes myEnum : values()) {
			keyTypeToNameMap.put(myEnum.getValue(), myEnum);
		}
	}
	
	private KeyTypes(final Integer newValue) {
		value = newValue;
	}
	
	public Integer getValue() { 
    	return value; 
    }
	
	public static KeyTypes getAPINameByValue(Integer value) {
        return keyTypeToNameMap.get(value);
    }
	
}
