package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Geotag {

    @JsonProperty("lon")
    public String mLongitude;
    @JsonProperty("lat")
    public String mLatitude;

    @Override
    public String toString() {
        return "Geotag{" +
                "mLongitude='" + mLongitude + '\'' +
                ", mLatitude='" + mLatitude + '\'' +
                '}';
    }
}
