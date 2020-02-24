package com.vassarlabs.proj.uniapp.enums;

import java.util.HashMap;
import java.util.Map;

public enum DisplayLabels {
	
	MASTER(0),
	PRESENT(1),
	CUMULATIVE(2),
	COMPUTED(3);
	
	private static final Map<Integer, DisplayLabels> DisplayLabelTypeToNameMap = new HashMap<Integer, DisplayLabels>();
	private final Integer value;
	
	static {
		for (DisplayLabels myEnum : values()) {
			DisplayLabelTypeToNameMap.put(myEnum.getValue(), myEnum);
		}
	}
	
	private DisplayLabels(final Integer newValue) {
		value = newValue;
	}
	
	public Integer getValue() {
		return value;
	}
	
}
