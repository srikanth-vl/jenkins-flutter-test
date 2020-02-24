package com.vassar.unifiedapp.listener;

import java.util.Map;

public interface ProjectTypeServiceListener {
    void onProjectTypeTaskCompleted(String projectTypeString);
    void onProjectTypeTaskFailed(String message);
    void onTokenExpired(Map<String, String> params);
}
