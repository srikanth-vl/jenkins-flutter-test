package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SplashScreenProperties {
    @JsonProperty("splashduration")
    public String mSplashDuration;
    @JsonProperty("splashicon")
    public String mSplashIconUrl;
    @JsonProperty("loginicon")
    public String mLoginIcon;
    @JsonProperty("splashbackground")
    public String mSplashBackground;

    public SplashScreenProperties() {
    }

    public SplashScreenProperties(String splashDuration, String splashIconUrl
            , String loginIcon, String splashBackground) {
        this.mSplashDuration = splashDuration;
        this.mSplashIconUrl = splashIconUrl;
        this.mLoginIcon = loginIcon;
        this.mSplashBackground = splashBackground;
    }

    @Override
    public String toString() {
        return "SplashScreenProperties{" +
                "mSplashDuration='" + mSplashDuration + '\'' +
                ", mSplashIconUrl='" + mSplashIconUrl + '\'' +
                ", mLoginIcon='" + mLoginIcon + '\'' +
                ", mSplashBackground='" + mSplashBackground + '\'' +
                '}';
    }
}