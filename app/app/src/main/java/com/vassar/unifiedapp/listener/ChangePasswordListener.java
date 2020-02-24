package com.vassar.unifiedapp.listener;

public interface ChangePasswordListener {
    void onChangePasswordSuccessful();
    void onChangePasswordFailure(String message);
}
