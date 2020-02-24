package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageInfo implements Parcelable {

    public LanguageInfo() {
    }

    public static final Creator<LanguageInfo> CREATOR = new Creator<LanguageInfo>() {
        @Override
        public LanguageInfo createFromParcel(Parcel in) {
            return new LanguageInfo(in);
        }

        @Override
        public LanguageInfo[] newArray(int size) {
            return new LanguageInfo[size];
        }
    };
    @JsonProperty("name")
    private String name;
    @JsonProperty("locale")
    private String locale;

    public LanguageInfo(String name, String locale) {
        this.locale = locale;
        this.name = name;
    }

    protected LanguageInfo(Parcel in) {
        locale = in.readString();
        name = in.readString();
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(locale);
    }
}
