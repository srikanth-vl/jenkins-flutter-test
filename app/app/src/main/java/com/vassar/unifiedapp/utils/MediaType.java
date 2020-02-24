package com.vassar.unifiedapp.utils;
import java.util.HashMap;
import java.util.Map;

public enum MediaType {

    IMAGE(0),
    VIDEO(1),
    AUDIO(2),
    TEXT(3),
    PDF(4),
    BLOB(5),
    OTHERS(6);


    private static final Map<Integer, MediaType> valueToTypeMap = new HashMap<>();
    private final Integer value;

    static {
        for (MediaType myEnum : values()) {
            valueToTypeMap.put(myEnum.getValue(), myEnum);
        }
    }

    private MediaType(final Integer newValue) {
        value = newValue;
    }

    public Integer getValue() {
        return value;
    }

    public static MediaType getTypeByValue(Integer value) {
        return valueToTypeMap.get(value);
    }
}
