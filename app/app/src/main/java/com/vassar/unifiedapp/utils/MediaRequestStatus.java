package com.vassar.unifiedapp.utils;
import java.util.HashMap;
import java.util.Map;

public enum MediaRequestStatus {

    NEW_STATUS(0),
    SUCCESS_STATUS(1),
    FAILURE_STATUS(2),
    PENDING_RETRY_STATUS(3),
    DELETED(4);


    private static final Map<Integer, MediaRequestStatus> valueToRequestStatusNameMap = new HashMap<>();
    private final Integer value;

    static {
        for (MediaRequestStatus myEnum : values()) {
            valueToRequestStatusNameMap.put(myEnum.getValue(), myEnum);
        }
    }

    private MediaRequestStatus(final Integer newValue) {
        value = newValue;
    }

    public Integer getValue() {
        return value;
    }

    public static MediaRequestStatus getSubTypeByValue(Integer value) {
        return valueToRequestStatusNameMap.get(value);
    }
}
