package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectListFieldModel implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ProjectListFieldModel createFromParcel(Parcel in) {
            return new ProjectListFieldModel(in);
        }

        public ProjectListFieldModel[] newArray(int size) {
            return new ProjectListFieldModel[size];
        }
    };
    @JsonProperty("key")
    public String mIdentifier;
    @JsonProperty("value")
    public ProjectListFieldValue mProjectListFieldValue;

    public ProjectListFieldModel() {
    }

    public ProjectListFieldModel(String mIdentifier, ProjectListFieldValue value) {
        this.mIdentifier = mIdentifier;
        this.mProjectListFieldValue = value;
    }

    // Parcelling part
    public ProjectListFieldModel(Parcel in) {
        this.mIdentifier = in.readString();
        this.mProjectListFieldValue = in.readParcelable(ProjectListFieldValue.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mIdentifier);
        dest.writeParcelable(this.mProjectListFieldValue, flags);
    }
}
