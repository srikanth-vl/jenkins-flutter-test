package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectTypeConfiguration implements Serializable {
    @JsonProperty("currenttime")
    public long mCurrentServerTime;
    @JsonProperty("user_id")
    public String mUserId;
    @JsonProperty("projecttype")
    public String mProjectTypeId;
    @JsonProperty("deptname")
    public String mDepartmentId;

    /* Map*<Project_Id, <Form_Type>, ProjectSpecificForms > */
    @JsonProperty("content")
    public Map<String, Map<String, ProjectSpecificForms>> mContent;

    @Override
    public String toString() {
        return "ProjectTypeConfiguration{" +
                "mCurrentServerTime=" + mCurrentServerTime +
                ", mUserId='" + mUserId + '\'' +
                ", mProjectTypeId='" + mProjectTypeId + '\'' +
                ", mDepartmentId='" + mDepartmentId + '\'' +
                ", mContent=" + mContent +
                '}';
    }
}
