package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSpecificForms {
    @JsonProperty("formversion")
    public int mFormVerion;
    @JsonProperty("is_active")
    public int mIsActive;
    @JsonProperty("mdinstanceid")
    public String mMetaDataInstanceId;
    @JsonProperty("forminstanceid")
    public String mProjectFormId;
    @JsonProperty("forms")
    public ActionForms mActionForms;
}
