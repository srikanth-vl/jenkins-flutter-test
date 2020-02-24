package com.vassar.unifiedapp.listener;

public interface AppMetaConfigServiceListener {
    void AppMetaConfigTaskCompleted(String appMetaConfig);
    void AppMetaConfigTaskFailed(String message);
}
