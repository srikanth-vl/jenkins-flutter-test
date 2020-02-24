package com.vassar.unifiedapp.listener;

import com.vassar.unifiedapp.model.FormImage;

public interface OnFormTaskCompletedListener {
    void onTaskCompleted(String response, String appId, String userId, FormImage formImage);
}
