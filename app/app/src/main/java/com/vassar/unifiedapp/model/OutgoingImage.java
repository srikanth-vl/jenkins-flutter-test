package com.vassar.unifiedapp.model;

public class OutgoingImage {

    public String mAppId;
    public String mUserId;
    public String mUUID;
    public String mTimestamp;
    public String mProjectId;
    public String mLocalPath;
    public String mKey;

    public OutgoingImage(String appId, String userId, String uuid
            , String timestamp, String projectId, String localPath
            , String key) {
        this.mAppId = appId;
        this.mUserId = userId;
        this.mUUID = uuid;
        this.mTimestamp = timestamp;
        this.mProjectId = projectId;
        this.mLocalPath = localPath;
        this.mKey = key;
    }
}
