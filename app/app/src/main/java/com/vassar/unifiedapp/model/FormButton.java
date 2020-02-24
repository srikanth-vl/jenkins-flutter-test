package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormButton {
    @JsonProperty("key")
    public String mIdentifier;
    @JsonProperty("label")
    public String mLabel;
    @JsonProperty("expandable")
    public ExpandableComponent mExpandable;
    @JsonProperty("api")
    public String mApi;
}
