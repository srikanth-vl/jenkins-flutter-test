package com.vassar.unifiedapp.utils;
import java.util.HashMap;
import java.util.Map;

public enum MediaActionType {

    UPLOAD(1),
    DOWNLOAD(0);

    private static final Map<Integer, MediaActionType> valueToActionNameMap = new HashMap<>();
    private final Integer value;

    static {
        for (MediaActionType myEnum : values()) {
            valueToActionNameMap.put(myEnum.getValue(), myEnum);
        }
    }

    private MediaActionType(final Integer newValue) {
        value = newValue;
    }

    public Integer getValue() {
        return value;
    }

    public static MediaActionType getActionByValue(Integer value) {
        return valueToActionNameMap.get(value);
    }
}
