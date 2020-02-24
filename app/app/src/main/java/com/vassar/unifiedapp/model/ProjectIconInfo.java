package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectIconInfo implements Parcelable {

    public static final Creator CREATOR = new Creator() {
        public ProjectIconInfo createFromParcel(Parcel in) {
            return new ProjectIconInfo(in);
        }

        public ProjectIconInfo[] newArray(int size) {
            return new ProjectIconInfo[size];
        }
    };
    @JsonProperty("static_url")
    public String mStaticUrl;
    @JsonProperty("dynamic_key_name")
    public String mDynamicKeyName;

    public ProjectIconInfo() {
    }

    public ProjectIconInfo(String label, String value) {
        this.mStaticUrl = label;
        this.mDynamicKeyName = value;
    }

    // Parcelling part
    public ProjectIconInfo(Parcel in) {
        this.mStaticUrl = in.readString();
        this.mDynamicKeyName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mStaticUrl);
        dest.writeString(this.mDynamicKeyName);
    }
}
