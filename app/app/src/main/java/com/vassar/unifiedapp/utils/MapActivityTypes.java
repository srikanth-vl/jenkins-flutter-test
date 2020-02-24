package com.vassar.unifiedapp.utils;

import java.util.HashMap;
import java.util.Map;

public enum MapActivityTypes {

    STATIC_MARKER(1),
    DROP_MARKER(2),
    DRAG_AND_DROP(3);

    private static final Map<Integer, MapActivityTypes> valueToActionNameMap = new HashMap<>();
    private final Integer value;

    static {
        for (MapActivityTypes myEnum : values()) {
            valueToActionNameMap.put(myEnum.getValue(), myEnum);
        }
    }

    private MapActivityTypes(final Integer newValue) {
        value = newValue;
    }

    public Integer getValue() {
        return value;
    }

    public static MapActivityTypes getActionByValue(Integer value) {
        return valueToActionNameMap.get(value);
    }
}
