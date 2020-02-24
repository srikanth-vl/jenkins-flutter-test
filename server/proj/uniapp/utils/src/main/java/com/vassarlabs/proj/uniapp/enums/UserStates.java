package com.vassarlabs.proj.uniapp.enums;

import java.util.HashMap;
import java.util.Map;

public enum UserStates {
	ACTIVE( true),
	INACTIVE(false);
	
	private static final Map<Boolean, UserStates> keyTypeToNameMap = new HashMap<Boolean, UserStates>();
	private final boolean value;
	
	static {
		for (UserStates myEnum : values()) {
			keyTypeToNameMap.put(myEnum.getValue(), myEnum);
		}
	}
	
	private UserStates(final boolean newValue) {
		value = newValue;
	}
	
	public boolean getValue() { 
    	return value; 
    }
	
	public static UserStates getAPINameByValue(boolean value) {
        return keyTypeToNameMap.get(value);
    }
	
}
