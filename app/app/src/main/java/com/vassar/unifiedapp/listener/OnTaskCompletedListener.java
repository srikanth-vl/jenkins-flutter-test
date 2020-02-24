package com.vassar.unifiedapp.listener;

import com.vassar.unifiedapp.model.OutgoingImage;

public interface OnTaskCompletedListener {
    void onTaskCompleted(String response, String appId, String userId, OutgoingImage outgoingImage);
}
