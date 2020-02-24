package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableRow {
    @JsonProperty("weightsum")
    public int mWeightSum;
    @JsonProperty("background")
    public String mBackgroundColor;
    @JsonProperty("foreground")
    public String mForegroundColor;
    @JsonProperty("components")
    public List<FormField> mRowComponents;
    @JsonProperty("repeat")
    public boolean mRepeat;
    @JsonProperty("filtering_key")
    public String mFilteringKey;
}
