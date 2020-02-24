package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationValues {

    @JsonProperty("min")
    public String mMin;
    @JsonProperty("max")
    public String mMax;

//    @Override
//    public String toString() {
//        return "ValidationValues {" +
//                "min='" + mMin + '\'' +
//                ", max=" + mMax +
//                '}';
//    }
}
