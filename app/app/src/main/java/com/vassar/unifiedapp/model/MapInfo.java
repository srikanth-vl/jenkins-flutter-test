package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapInfo implements Parcelable {
    public static final Creator<MapInfo> CREATOR = new Creator<MapInfo>() {
        @Override
        public MapInfo createFromParcel(Parcel in) {
            return new MapInfo(in);
        }

        @Override
        public MapInfo[] newArray(int size) {
            return new MapInfo[size];
        }
    };
    @JsonProperty("map_entity_name")
    private String mapEntityName;
    @JsonProperty("map_entity_additional_info")
    private Map<String, String> additionalInfo;
    @JsonProperty("download_url")
    private String downloadUrl;
    @JsonProperty("has_toggle")
    private boolean hasToggle;
    @JsonProperty("icon_url")
    private String iconUrl;

    public MapInfo() {
    }

    public MapInfo(String mapEntityName, Map<String, String> additionalInfo, String downloadUrl, boolean hasToggle, String iconUrl) {
        this.mapEntityName = mapEntityName;
        this.additionalInfo = additionalInfo;
        this.downloadUrl = downloadUrl;
        this.hasToggle = hasToggle;
        this.iconUrl = iconUrl;
    }

    protected MapInfo(Parcel in) {
        mapEntityName = in.readString();
        int size = in.readInt();
        this.additionalInfo = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            this.additionalInfo.put(key, value);
        }
        downloadUrl = in.readString();
        hasToggle = in.readByte() != 0;
        iconUrl = in.readString();
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isHasToggle() {
        return hasToggle;
    }

    public void setHasToggle(boolean hasToggle) {
        this.hasToggle = hasToggle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getMapEntityName() {
        return mapEntityName;
    }

    public void setMapEntityName(String mapEntityName) {
        this.mapEntityName = mapEntityName;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mapEntityName);
        if (additionalInfo != null) {
            dest.writeInt(additionalInfo.size());
            for (Map.Entry<String, String> entry : additionalInfo.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
        dest.writeString(downloadUrl);
        dest.writeByte((byte) (hasToggle ? 1 : 0));
        dest.writeString(iconUrl);
    }

    @Override
    public String toString() {
        return "MapInfo{" +
                "mapEntityName='" + mapEntityName + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                ", downloadUrl=" + downloadUrl +
                ", hasToggle=" + hasToggle +
                ", iconurl=" + iconUrl +
                '}';
    }
}
