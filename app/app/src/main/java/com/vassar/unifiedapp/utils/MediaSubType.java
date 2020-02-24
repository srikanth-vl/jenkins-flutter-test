package com.vassar.unifiedapp.utils;
import java.util.HashMap;
import java.util.Map;

public enum MediaSubType {

    FULL(0),
    PREVIEW(1),
    THUMBNAIL(2);


    private static final Map<Integer, MediaSubType> valueToSubTypeNameMap = new HashMap<>();
    private final Integer value;

    static {
        for (MediaSubType myEnum : values()) {
            valueToSubTypeNameMap.put(myEnum.getValue(), myEnum);
        }
    }

    private MediaSubType(final Integer newValue) {
        value = newValue;
    }

    public Integer getValue() {
        return value;
    }

    public static MediaSubType getSubTypeByValue(Integer value) {
        return valueToSubTypeNameMap.get(value);
    }
}
