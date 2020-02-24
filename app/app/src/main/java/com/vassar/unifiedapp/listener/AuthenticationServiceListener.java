package com.vassar.unifiedapp.listener;

import com.vassar.unifiedapp.model.UserMetaData;

public interface AuthenticationServiceListener {
    void onAuthenticationSuccessful(UserMetaData userMetaData);
    void onAuthenticationFailure(String message);
}
