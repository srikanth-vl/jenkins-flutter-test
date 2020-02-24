package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalizationConfig implements Parcelable{
    @JsonProperty("currenttime")
    public long mCurrentServerTime;
    @JsonProperty("user_id")
    public String mUserId;
    @JsonProperty("version")
    public int mVersion;
    @JsonProperty("config")
    public String mConfig;

    public LocalizationConfig() { }

    public LocalizationConfig(long currentServerTime, String userId, int version, String config) {
        this.mCurrentServerTime = currentServerTime;
        this.mUserId = userId;
        this.mVersion = version;
        this.mConfig = config;
    }

    public static final Creator CREATOR = new Creator() {
        public LocalizationConfig createFromParcel(Parcel in) {
            return new LocalizationConfig(in);
        }

        public LocalizationConfig[] newArray(int size) {
            return new LocalizationConfig[size];
        }
    };

    // Parcelling part
    public LocalizationConfig(Parcel in){
        this.mCurrentServerTime = in.readLong();
        this.mUserId =  in.readString();
        this.mConfig =  in.readString();
        this.mVersion =  in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mCurrentServerTime);
        dest.writeString(this.mUserId);
        dest.writeString(this.mConfig);
        dest.writeInt(this.mVersion);
    }
}
