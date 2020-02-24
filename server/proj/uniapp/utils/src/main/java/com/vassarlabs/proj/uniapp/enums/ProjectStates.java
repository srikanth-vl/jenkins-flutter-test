package com.vassarlabs.proj.uniapp.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ProjectStates {
	
	INPROGRESS("In Progress"),
	DELETED("Deleted"),
	NEW("New"),
	ALL("All");
	
	private static final Map<String, ProjectStates> projectStateToEnumMap = new HashMap<String, ProjectStates>();
	private final String value;
	
	static {
	    for (ProjectStates myEnum : values()) {
	      projectStateToEnumMap.put(myEnum.getValue(), myEnum);
	    }
    }
	ProjectStates(final String newValue) {
        value = newValue;
    }

    public String getValue() { 
    	return value; 
    }
      
    public static ProjectStates getProjectStateNameByValue(String value) {
        return projectStateToEnumMap.get(value);
    }
    
    public static ProjectStates getDefaultProjectState() {
        return ProjectStates.INPROGRESS;
    }
    /**
     * Gets list of valid project states
     * @return
     */
    public static List<ProjectStates> getValidProjectStates() {
    	List<ProjectStates> projectStates = new ArrayList<>();
    	projectStates.add(INPROGRESS);
    	projectStates.add(NEW);
    	return projectStates;
    }
}
