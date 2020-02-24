package com.vassarlabs.proj.uniapp.enums;

import java.util.HashMap;
import java.util.Map;

public enum MediaRelayStates {
	
	FORM_DATA_RELAYED(1),
	FILE_DATA_RALYED(2),
	DEFAULT(0);
	
	private static final Map<Integer, MediaRelayStates> keyTypeToNameMap = new HashMap<Integer, MediaRelayStates>();
	private final Integer value;
	
	static {
		for (MediaRelayStates myEnum : values()) {
			keyTypeToNameMap.put(myEnum.getValue(), myEnum);
		}
	}
	
	private MediaRelayStates(final Integer newValue) {
		value = newValue;
	}
	
	public Integer getValue() { 
    	return value; 
    }
	
	public static MediaRelayStates getAPINameByValue(Integer value) {
        return keyTypeToNameMap.get(value);
    }
	

}