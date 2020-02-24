package com.vassar.unifiedapp.listener;

import java.util.Map;

public interface RootConfigServiceListener {
    void onRootConfigTaskCompleted(String rootConfigString);
    void onRootConfigTaskFailed(String message);
    void onTokenExpired(Map<String, String> params);
}
