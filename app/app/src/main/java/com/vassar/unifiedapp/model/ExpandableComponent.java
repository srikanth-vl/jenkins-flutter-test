package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpandableComponent {
    @JsonProperty("type")
    public int mType;
    @JsonProperty("icon_url")
    public String mIconUrl;
    @JsonProperty("subform")
    public String mSubForm;
    @JsonProperty("text")
    public String mText;
}
