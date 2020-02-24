package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppColorScheme {
    @JsonProperty("colorprimary")
    public String mColorPrimary;
    @JsonProperty("colorprimarydark")
    public String mColorPrimaryDark;
    @JsonProperty("coloraccent")
    public String mColorAccent;

    public AppColorScheme() {
    }

    public AppColorScheme(String colorPrimary, String colorPrimaryDark, String colorAccent) {
        this.mColorPrimary = colorPrimary;
        this.mColorPrimaryDark = colorPrimaryDark;
        this.mColorAccent = colorAccent;
    }

    @Override
    public String toString() {
        return "AppColorScheme{" +
                "mColorPrimary='" + mColorPrimary + '\'' +
                ", mColorPrimaryDark='" + mColorPrimaryDark + '\'' +
                ", mColorAccent='" + mColorAccent + '\'' +
                '}';
    }
}
