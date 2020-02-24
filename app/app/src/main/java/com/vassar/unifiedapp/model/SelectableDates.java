package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectableDates {
    @JsonProperty("past")
    public String mPast;
    @JsonProperty("future")
    public String mFuture;
    @JsonProperty("select")
    public String mSelect;
    @JsonProperty("interval")
    public String mInterval;
    @JsonProperty("start")
    public String mStart;
    @JsonProperty("end")
    public String mEnd;
}
