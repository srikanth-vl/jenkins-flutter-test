package com.vassarlabs.proj.uniapp.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum FormTypes {
	
	INSERT(0),
	UPDATE(1);
	
	private static final Map<Integer, FormTypes> keyTypeToNameMap = new HashMap<Integer, FormTypes>();
	private final Integer value;
	
	static {
		for (FormTypes myEnum : values()) {
			keyTypeToNameMap.put(myEnum.getValue(), myEnum);
		}
	}
	
	private FormTypes(final Integer newValue) {
		value = newValue;
	}
	
	public Integer getValue() { 
    	return value; 
    }
	
	public static FormTypes getFormNameByValue(Integer value) {
        return keyTypeToNameMap.get(value);
    }
	
	public static List<String> getValidFormTypes() {
		List<String> formTypes = new ArrayList<>();
		formTypes.add(INSERT.name());
		formTypes.add(UPDATE.name());
		return formTypes;
	}
}
