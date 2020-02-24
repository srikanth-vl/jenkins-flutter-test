package com.vassar.unifiedapp.application;

import java.util.HashMap;
import java.util.Map;

public class AppContext {
    private static final AppContext ourInstance = new AppContext();

    public static AppContext getInstance() {
        return ourInstance;
    }

    private static Map<String, Object> appValues;

    private AppContext() {
        appValues = new HashMap<>();
    }

    public void addToContext(String key, Object value) {
        appValues.put(key, value);
    }

    public Object getFromContext(String key) {
        return appValues.get(key);
    }
}
