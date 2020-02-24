package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipleValues {
    @JsonProperty("value")
    public String mValue;
    @JsonProperty("expandable")
    public ExpandableComponent mExpandable;
}
