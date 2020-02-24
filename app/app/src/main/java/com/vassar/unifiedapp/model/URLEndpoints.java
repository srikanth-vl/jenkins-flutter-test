package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
class URLEndpoints {
    @JsonProperty("rootconfig")
    public String mRootConfigEndpoint;
    @JsonProperty("login")
    public String mLoginEndpoint;
    @JsonProperty("projecttype")
    public String mProjectTypeEndpoint;
    @JsonProperty("projectlist")
    public String mProjectListEndpoint;
    @JsonProperty("syncdata")
    public String mSyncDataEndpoint;
    @JsonProperty("logout")
    public String mLogoutEndpoint;

    public URLEndpoints() {
    }

    @Override
    public String toString() {
        return "URLEndpoints{" +
                "mRootConfigEndpoint='" + mRootConfigEndpoint + '\'' +
                ", mLoginEndpoint='" + mLoginEndpoint + '\'' +
                ", mProjectTypeEndpoint='" + mProjectTypeEndpoint + '\'' +
                ", mProjectListEndpoint='" + mProjectListEndpoint + '\'' +
                ", mSyncDataEndpoint='" + mSyncDataEndpoint + '\'' +
                ", mLogoutEndpoint='" + mLogoutEndpoint + '\'' +
                '}';
    }
}
