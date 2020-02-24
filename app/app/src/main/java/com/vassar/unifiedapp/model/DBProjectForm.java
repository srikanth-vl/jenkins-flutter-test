package com.vassar.unifiedapp.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DBProjectForm implements Serializable {

    @JsonProperty("user_id")
    public String mUserId;
    @JsonProperty("app_id")
    public String mAppId;
    @JsonProperty("project_id")
    public String mProjectId;
    @JsonProperty("form_type")
    public String mFormType;
    @JsonProperty("form_data")
    public String mFormData;
    @JsonProperty("form_version")
    public int mFormVersion;
    @JsonProperty("md_instance_id")
    public String mMdInstanceId;

    public DBProjectForm() { }

    public void setformType(String formType) {
        this.mFormType = formType;
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

    public String getProjectId() {
        return mProjectId;
    }

    public void setProjectId(String projectId) {
        this.mProjectId = projectId;
    }

    public String getformType() {
        return mFormType;
    }

    public String getFormData() {
        return mFormData;
    }

    public void setFormData(String formData) {
        this.mFormData = formData;
    }

    public int getFormVersion() {
        return mFormVersion;
    }

    public void setFormVersion(int formVersion) {
        this.mFormVersion = formVersion;
    }

    public String getMdInstanceId() {
        return mMdInstanceId;
    }

    public void setMdInstanceId(String mMdInstanceId) {
        this.mMdInstanceId = mMdInstanceId;
    }
}
