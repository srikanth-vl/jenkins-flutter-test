package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vassar.unifiedapp.utils.CustomDeserialiser;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OnStartupAction {
    @JsonProperty("action_type")
    private String type;
    @JsonDeserialize(using = CustomDeserialiser.class)
    @JsonProperty("action_md")
    private String metadata;

    public OnStartupAction() {
    }

    @Override
    public String toString() {
        return "OnStartupAction{" +
                "type='" + type + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
