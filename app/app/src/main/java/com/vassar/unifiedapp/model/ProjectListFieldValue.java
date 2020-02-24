package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectListFieldValue implements Parcelable {

    @JsonProperty("label")
    public String mLabel;
    @JsonProperty("value")
    public String mValue;

    public ProjectListFieldValue() { }

    public ProjectListFieldValue(String label, String value) {
        this.mLabel = label;
        this.mValue = value;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ProjectListFieldValue createFromParcel(Parcel in) {
            return new ProjectListFieldValue(in);
        }

        public ProjectListFieldValue[] newArray(int size) {
            return new ProjectListFieldValue[size];
        }
    };

    // Parcelling part
    public ProjectListFieldValue(Parcel in){
        this.mLabel = in.readString();
        this.mValue =  in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLabel);
        dest.writeString(this.mValue);
    }
}
