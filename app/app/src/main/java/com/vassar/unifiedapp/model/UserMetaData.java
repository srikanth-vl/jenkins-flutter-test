package com.vassar.unifiedapp.model;

public class UserMetaData {

    public String mPassword;
    public String mUserId;
    public String mToken;
    public Long mTimestamp;
    public boolean mIsLoggedIn;
    public Long mLoginTs;
    public String userDetails;

    public UserMetaData(String password, String userId, String token
            , long timestamp, boolean isLoggedIn, Long mLoginTs, String userDetails) {
        this.mPassword = password;
        this.mUserId = userId;
        this.mToken = token;
        this.mTimestamp = timestamp;
        this.mIsLoggedIn = isLoggedIn;
        this.mLoginTs = mLoginTs;
        this.userDetails = userDetails;
    }

    @Override
    public String toString() {
        return "UserMetaData{" +
                "mPassword='" + mPassword + '\'' +
                ", mUserId='" + mUserId + '\'' +
                ", mToken='" + mToken + '\'' +
                ", mTimestamp=" + mTimestamp +
                ", mIsLoggedIn=" + mIsLoggedIn +
                ", mLoginTs=" + mLoginTs +
                ", userDetails=" + userDetails +
                '}';
    }
}
