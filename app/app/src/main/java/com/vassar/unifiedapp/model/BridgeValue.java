package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BridgeValue {
    @JsonProperty("value")
    public String mValue;
    @JsonProperty("bg-color")
    public String mBackgroundColor;
    @JsonProperty("expandable")
    public ExpandableComponent mExpandableComponent;
    @JsonProperty("mandatory_fields")
    public ArrayList<String> mMandatoryFields;
}
