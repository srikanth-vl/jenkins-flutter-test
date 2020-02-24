package com.vassar.unifiedapp.model;

public class AuthenticationResponse {

    private boolean mIsAuthenticated;
    private String mMessage;
    private UserMetaData mUserMetaData;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(boolean isAuthenticated, String message, UserMetaData userMetaData) {
        this.mIsAuthenticated = isAuthenticated;
        this.mMessage = message;
        this.mUserMetaData = userMetaData;
    }

    public boolean isAuthenticated() {
        return mIsAuthenticated;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.mIsAuthenticated = isAuthenticated;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public UserMetaData getUserMetaData() {
        return mUserMetaData;
    }

    public void setUserMetaData(UserMetaData userMetaData) {
        this.mUserMetaData = userMetaData;
    }
}
