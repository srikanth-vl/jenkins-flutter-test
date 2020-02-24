package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Header {
    @JsonProperty("key")
    public String mIdentifier;
    @JsonProperty("label")
    public String mValue;
    @JsonProperty("icon")
    public String mIconUrl;
    @JsonProperty("display")
    public boolean mDisplay;
    @JsonProperty("submittable")
    public boolean mSubmittable;
    @JsonProperty("aligned")
    public String mAligned;
    @JsonProperty("new_row")
    public boolean mNewRow;

    @Override
    public String toString() {
        return "Header{" +
                "mIdentifier='" + mIdentifier + '\'' +
                ", mValue='" + mValue + '\'' +
                ", mIconUrl='" + mIconUrl + '\'' +
                ", mDisplay=" + mDisplay +
                ", mSubmittable=" + mSubmittable +
                ", mAligned='" + mAligned + '\'' +
                ", mNewRow=" + mNewRow +
                '}';
    }
}
