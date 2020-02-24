package com.vassar.unifiedapp.listener;

import java.util.Map;

public interface ProjectListServiceListener {
    void onProjectListTaskCompleted(String projectListString);
    void onProjectListTaskFailed(String message);
    void onTokenExpired(Map<String, Object> params);
}
