package com.vassar.unifiedapp.listener;

import com.vassar.unifiedapp.model.FormButton;

import java.util.Map;

public interface SubmitProjectServiceListener {
    void onSubmitTaskCompleted(String submitString);
    void onSubmitTaskFailed(String message);
    void onTokenExpired(Map<String, String> params, FormButton formButton);
    void onValidationException(String message);
}
