package com.vassarlabs.proj.uniapp.enums;

import java.util.HashMap;
import java.util.Map;

public enum ActiveFlags {
	
	ACTIVE(0),
	INACTIVE(1),
	ALL(-1);
	
	private static final Map<Integer, ActiveFlags> keyTypeToNameMap = new HashMap<Integer, ActiveFlags>();
	private final Integer value;
	
	static {
		for (ActiveFlags myEnum : values()) {
			keyTypeToNameMap.put(myEnum.getValue(), myEnum);
		}
	}
	
	private ActiveFlags(final Integer newValue) {
		value = newValue;
	}
	
	public Integer getValue() { 
    	return value; 
    }
	
	public static ActiveFlags getAPINameByValue(Integer value) {
        return keyTypeToNameMap.get(value);
    }
	

}