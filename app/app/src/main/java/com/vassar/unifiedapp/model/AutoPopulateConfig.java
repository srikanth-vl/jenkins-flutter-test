package com.vassar.unifiedapp.model;

import android.view.View;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoPopulateConfig {
    @JsonProperty("source")
    public Boolean isSource;
    @JsonProperty("source_key")
    public String mSourceKey;
    @JsonProperty("api")
    public String mApi;
    @JsonProperty("resource_location")
    public String mResourceLocation;

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    @JsonProperty("expression")
    public boolean mExpression;
    @JsonProperty("entity_name")
    public String mEntityName;
    @JsonProperty("dimension_name")
    public String mDimensionName;
    @JsonIgnore
    public View mView;
    @JsonIgnore
    public String mUiType;
    @JsonIgnore
    public String mTag;

    public Boolean getSource() {
        return isSource;
    }

    public void setSource(Boolean source) {
        isSource = source;
    }

    public String getSourceKey() {
        return mSourceKey;
    }

    public void setSourceKey(String mSourceKey) {
        this.mSourceKey = mSourceKey;
    }

    public String getApi() {
        return mApi;
    }

    public void setApi(String mApi) {
        this.mApi = mApi;
    }

    public String getResourceLocation() {
        return mResourceLocation;
    }

    public void setResourceLocation(String mResourceLocation) {
        this.mResourceLocation = mResourceLocation;
    }

    public boolean isExpression() {
        return mExpression;
    }

    public void setExpression(boolean mExpression) {
        this.mExpression = mExpression;
    }

    public String getmDimensionName() {
        return mDimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.mDimensionName = dimensionName;
    }
}
