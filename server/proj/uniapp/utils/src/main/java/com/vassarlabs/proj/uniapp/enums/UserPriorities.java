package com.vassarlabs.proj.uniapp.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vassarlabs.proj.uniapp.constants.CommonConstants;

public enum UserPriorities {

	Default(0),
	Primary(1),
	Secondary(2);
	
	private static final Map<Integer, UserPriorities> userTypeToPriorityMap = new HashMap<Integer, UserPriorities>();
	private final Integer value;
	
	static {
	    for (UserPriorities myEnum : values()) {
	      userTypeToPriorityMap.put(myEnum.getValue(), myEnum);
	    }
    }
	UserPriorities(final Integer newValue) {
        value = newValue;
    }

    public Integer getValue() { 
    	return value; 
    }	
      
    public static UserPriorities getAPINameByValue(Integer value) {
        return userTypeToPriorityMap.get(value);
    }
    
    public static int convertUserTypeToInt(String userTypeStr) {
		if(userTypeStr.toLowerCase().contains(Primary.name().toLowerCase())) {
			return Primary.getValue();
		} else if(userTypeStr.toLowerCase().contains(Secondary.name().toLowerCase())) {
			return Secondary.getValue();
		} else if(userTypeStr.toLowerCase().contains(Default.name().toLowerCase())) {
			return Default.getValue();
		} else {
			return CommonConstants.NAInteger;
		}
	}
    public static List<Integer> getAllValues() {
    	List<Integer> userPriorityValues = new ArrayList<>();
		List<UserPriorities> userPriorities = Arrays.asList(values());
		for (UserPriorities userPriority : userPriorities) {
			userPriorityValues.add(userPriority.getValue());
		}
		return userPriorityValues;
    }
}
