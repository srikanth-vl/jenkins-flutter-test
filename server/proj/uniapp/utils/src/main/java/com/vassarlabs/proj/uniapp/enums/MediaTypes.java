package com.vassarlabs.proj.uniapp.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MediaTypes {
	IMAGE("image"),
	VIDEO("video"),
	THUMBNAIL("image_thumbnail"),
	ZIP("zip");
	
	private static final Map<String, MediaTypes> mediaTypeToEnumMap = new HashMap<String, MediaTypes>();
	private final String value;
	
	static {
	    for (MediaTypes myEnum : values()) {
	    	mediaTypeToEnumMap.put(myEnum.getValue(), myEnum);
	    }
    }
	MediaTypes(final String newValue) {
        value = newValue;
    }

    public String getValue() { 
    	return value; 
    }
      
    public static MediaTypes getMediaTypeByValue(String value) {
        return mediaTypeToEnumMap.get(value);
    }
    
    public static List<String> getMediaTypes() {
    	List<String> Media = new ArrayList<String>();
    	Media.add(IMAGE.value);
    	Media.add(VIDEO.value);
    	Media.add(ZIP.value);
	return Media;
    
    }
    
	
}
