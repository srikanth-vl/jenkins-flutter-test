package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormBridge {
    @JsonProperty("key")
    public String mKey;
    @JsonProperty("label")
    public String mLabel;
    @JsonProperty("bridgevalues")
    public ArrayList<BridgeValue> mBridgeValues;
}
