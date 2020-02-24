package com.vassarlabs.proj.uniapp.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ComputationTypes {
	
	SUM("sum"),
	AVERAGE("average"),
	MIN("min"),
	MAX("max"),
	COUNT("count");
	
	private static final Map<String, ComputationTypes> computationTypeToEnumMap = new HashMap<String, ComputationTypes>();
	private final String value;
	
	static {
	    for (ComputationTypes myEnum : values()) {
	    	computationTypeToEnumMap.put(myEnum.getValue(), myEnum);
	    }
    }
	ComputationTypes(final String newValue) {
        value = newValue;
    }

    public String getValue() {
    	return value; 
    }
      
    public static ComputationTypes getComputationTypeNameByValue(String value) {
        return computationTypeToEnumMap.get(value);
    }
    
    /**
     * Gets list of valid project states
     * @return
     */
    public static List<ComputationTypes> getValidComputationTypes() {
    	List<ComputationTypes> computationTypes = new ArrayList<>();
    	computationTypes.add(SUM);
    	computationTypes.add(AVERAGE);
    	computationTypes.add(MIN);
    	computationTypes.add(MAX);
    	return computationTypes;
    }

}
