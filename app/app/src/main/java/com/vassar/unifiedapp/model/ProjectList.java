package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

//import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectList implements Serializable, Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ProjectList createFromParcel(Parcel in) {
            return new ProjectList(in);
        }

        public ProjectList[] newArray(int size) {
            return new ProjectList[size];
        }
    };
    @JsonProperty("currenttime")
    public long mLastSyncTime;
    @JsonProperty("userid")
    public String mUserId;
    @JsonProperty("types")
    public ArrayList<String> mUserTypes;
    @JsonProperty("projects")
    public ArrayList<Project> mProjects;
    @JsonProperty("showmap")
    public boolean mShouldShowMap;

    public ProjectList() {
        mProjects = new ArrayList<>();
        mUserTypes = new ArrayList<>();
    }

    public ProjectList(long lastSyncTime, String userId, ArrayList<String> userTypes, ArrayList<Project> projects,
                       boolean shouldShowMap) {
        this.mLastSyncTime = lastSyncTime;
        this.mUserId = userId;
        this.mUserTypes.clear();
        this.mUserTypes.addAll(userTypes);
        this.mProjects.clear();
        this.mProjects.addAll(projects);
        this.mShouldShowMap = shouldShowMap;
    }

    // Parcelling part
    public ProjectList(Parcel in) {
        this.mLastSyncTime = in.readLong();
        this.mUserId = in.readString();
        this.mUserTypes = (ArrayList<String>) in.readSerializable();
        in.readTypedList(this.mProjects, Project.CREATOR);
        this.mShouldShowMap = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mLastSyncTime);
        dest.writeString(this.mUserId);
        dest.writeSerializable(this.mUserTypes);
        dest.writeTypedList(this.mProjects);
        dest.writeByte((byte) (this.mShouldShowMap ? 1 : 0));
    }
}
