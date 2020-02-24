package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class RootConfig implements Parcelable{
    @JsonProperty("currenttime")
    public long mCurrentServerTime;
    @JsonProperty("user_id")
    public String mUserId;
    @JsonProperty("version")
    public int mVersion;
    @JsonProperty("application")
    public List<ProjectTypeModel> mApplications;

    public RootConfig() { }

    public RootConfig(long currentServerTime, String userId, int version, ArrayList<ProjectTypeModel> applications) {
        this.mCurrentServerTime = currentServerTime;
        this.mUserId = userId;
        this.mVersion = version;
        this.mApplications = new ArrayList<>();
        this.mApplications.addAll(applications);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public RootConfig createFromParcel(Parcel in) {
            return new RootConfig(in);
        }

        public RootConfig[] newArray(int size) {
            return new RootConfig[size];
        }
    };

    // Parcelling part
    public RootConfig(Parcel in){
        this.mCurrentServerTime = in.readLong();
        this.mUserId =  in.readString();

        // TODO: this.mApplications is getting cleared somewhere and set to NULL
        // so instantiating explicitly
        if (this.mApplications == null) {
            this.mApplications = new ArrayList<>();
        }
        in.readTypedList(this.mApplications, ProjectTypeModel.CREATOR);
//        this.mApplications = in.readParcelable(ProjectTypeModel.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mCurrentServerTime);
        dest.writeString(this.mUserId);
        if (this.mApplications != null && this.mApplications.size() > 0)
            dest.writeTypedList(this.mApplications);
        else
            dest.writeTypedList(new ArrayList<>());
//        dest.writeArray(this.mApplications, flags);
    }
}
