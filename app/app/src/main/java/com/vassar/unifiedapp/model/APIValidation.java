package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIValidation implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public APIValidation createFromParcel(Parcel in) {
            return new APIValidation(in);
        }

        public APIValidation[] newArray(int size) {
            return new APIValidation[size];
        }
    };
    @JsonProperty("route")
    public String mRequestRoute;
    @JsonProperty("type")
    public String mRequestType;
    @JsonProperty("params")
    public String mRequestParams;

    // Parcelling part
    public APIValidation(Parcel in) {
        this.mRequestRoute = in.readString();
        this.mRequestType = in.readString();
        this.mRequestParams = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mRequestRoute);
        dest.writeString(this.mRequestType);
        dest.writeString(this.mRequestParams);
    }
}
