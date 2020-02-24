package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapConfiguration implements Parcelable {

    public static final Creator<MapConfiguration> CREATOR = new Creator<MapConfiguration>() {
        @Override
        public MapConfiguration createFromParcel(Parcel in) {
            return new MapConfiguration(in);
        }

        @Override
        public MapConfiguration[] newArray(int size) {
            return new MapConfiguration[size];
        }
    };
    @JsonProperty("project_list_map")
    private ProjectListMap projectListMap;
    @JsonProperty("download_mapping")
    private Map<String, String> downloadMapping;
    @JsonProperty("download_dimension")
    private String downloadDimension;

    public MapConfiguration() {
    }

    protected MapConfiguration(Parcel in) {
        this.projectListMap = in.readParcelable(ProjectListMap.class.getClassLoader());
        int size = in.readInt();
        this.downloadMapping = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            this.downloadMapping.put(key, value);
        }
        this.downloadDimension = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.projectListMap, flags);
        if (downloadMapping != null) {
            dest.writeInt(downloadMapping.size());
            for (Map.Entry<String, String> entry : downloadMapping.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
        dest.writeString(this.downloadDimension);
    }

    public ProjectListMap getProjectListMap() {
        return projectListMap;
    }

    public void setProjectListMap(ProjectListMap projectListMap) {
        this.projectListMap = projectListMap;
    }

    public Map<String, String> getDownloadMapping() {
        return downloadMapping;
    }

    public void setDownloadMapping(Map<String, String> downloadMapping) {
        this.downloadMapping = downloadMapping;
    }

    public String getDownloadDimension() {
        return downloadDimension;
    }

    public void setDownloadDimension(String downloadDimension) {
        this.downloadDimension = downloadDimension;
    }
}
