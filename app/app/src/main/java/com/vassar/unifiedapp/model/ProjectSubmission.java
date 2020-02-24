package com.vassar.unifiedapp.model;

import java.util.HashMap;
import java.util.Map;

public class ProjectSubmission {

    public String mUserId;
    public String mAppId;
    public String mFormId;
    public Long mTimestamp;
    public String mUserType;
    public String mProjectId;
    public String mFields;
    public String mApi;
    public String mMdInstanceId;
    public int mUploadStatus;
    public String mResponse;
    public Long mLastServerUploadTimestamp;
    public int mUploadRetryCount;
    private Map<String, String> additionalProps;

    public ProjectSubmission() {
        this.additionalProps = new HashMap<>();
    }

    public ProjectSubmission(String userId, String appId
            , String formId, Long timestamp, String userType
            , String projectId, String object, String api, String mdInstanceId, int uploadStatus, String response,
                             long lastServerUploadTimestamp, int retryCount, Map<String, String> additionalProps) {
        this.mUserId = userId;
        this.mAppId = appId;
        this.mFormId = formId;
        this.mTimestamp = timestamp;
        this.mUserType = userType;
        this.mProjectId = projectId;
        this.mFields = object;
        this.mApi = api;
        this.mMdInstanceId = mdInstanceId;
        this.mUploadStatus = uploadStatus;
        this.mLastServerUploadTimestamp = lastServerUploadTimestamp;
        this.mUploadRetryCount = retryCount;
        this.mResponse = response;
        this.additionalProps = additionalProps;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getAppId() {
        return mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
    }

    public String getFormId() {
        return mFormId;
    }

    public void setFormId(String formId) {
        this.mFormId = formId;
    }

    public Long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.mTimestamp = timestamp;
    }

    public String getUserType() {
        return mUserType;
    }

    public void setUserType(String userType) {
        this.mUserType = userType;
    }

    public String getProjectId() {
        return mProjectId;
    }

    public void setProjectId(String projectId) {
        this.mProjectId = projectId;
    }

    public String getFields() {
        return mFields;
    }

    public void setFields(String object) {
        this.mFields = object;
    }

    public String getApi() {
        return mApi;
    }

    public void setApi(String api) {
        this.mApi = api;
    }

    public String getMdInstanceId() {
        return mMdInstanceId;
    }

    public void setMdInstanceId(String mdInstanceId) {
        this.mMdInstanceId = mdInstanceId;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public Long getLastServerUploadTimestamp() {
        return mLastServerUploadTimestamp;
    }

    public void setLastServerUploadTimestamp(Long lastServerUploadTimestamp) {
        this.mLastServerUploadTimestamp = lastServerUploadTimestamp;
    }

    public int getUploadRetryCount() {
        return mUploadRetryCount;
    }

    public void setUploadRetryCount(int uploadRetryCount) {
        this.mUploadRetryCount = uploadRetryCount;
    }

    public int getUploadStatus() {
        return mUploadStatus;
    }

    public void setUploadStatus(int uploadStatus) {
        this.mUploadStatus = mUploadStatus;
    }

    public String getResponse() {
        return mResponse;
    }

    public void setResponse(String response) {
        this.mResponse = response;
    }

    public Map<String, String> getAdditionalProps() {
        return additionalProps;
    }

    public void setAdditionalProps(Map<String, String> additionalProps) {
        this.additionalProps = additionalProps;
    }


    @Override
    public String toString() {
        return "ProjectSubmission{" +
                "mUserId='" + mUserId + '\'' +
                ", mAppId='" + mAppId + '\'' +
                ", mFormId='" + mFormId + '\'' +
                ", mTimestamp=" + mTimestamp +
                ", mUserType='" + mUserType + '\'' +
                ", mProjectId='" + mProjectId + '\'' +
                ", mFields='" + mFields + '\'' +
                ", mApi='" + mApi + '\'' +
                ", mMdInstanceId='" + mMdInstanceId + '\'' +
                ", mUploadStatus=" + mUploadStatus +
                ", mResponse='" + mResponse + '\'' +
                ", mLastServerUploadTimestamp=" + mLastServerUploadTimestamp +
                ", mUploadRetryCount=" + mUploadRetryCount +
                ", mAdditionalProperties=" + additionalProps +
                '}';
    }
}
