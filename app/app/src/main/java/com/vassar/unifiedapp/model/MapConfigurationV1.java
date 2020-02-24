package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapConfigurationV1 implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MapConfigurationV1 createFromParcel(Parcel in) {
            return new MapConfigurationV1(in);
        }

        public MapConfigurationV1[] newArray(int size) {
            return new MapConfigurationV1[size];
        }
    };
    @JsonProperty("currenttime")
    public long currentServerTime;
    @JsonProperty("offline_map_files")
    public List<OfflineMapFile> files;
    @JsonProperty("min_zoom")
    public Integer minZoom;
    @JsonProperty("max_zoom")
    public Integer maxZoom;
    @JsonProperty("version")
    public Integer version;
    @JsonProperty("offline_map_source_name")
    public String mapSourceName;
    @JsonProperty("map_markers")
    public Map<String, List<MapInfo>> mapMarkers;
    @JsonProperty("bounding_box")
    public String boundingBox;


    public MapConfigurationV1() {
    }

    public MapConfigurationV1(long currentServerTime, List<OfflineMapFile> files, Integer minZoom, Integer maxZoom,
                              Integer version, String mapSourceName, Map<String, List<MapInfo>> mapMarkers, String boundingBox) {
        this.currentServerTime = currentServerTime;
        this.files = new ArrayList<>();
        this.files.addAll(files);
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.version = version;
        this.mapSourceName = mapSourceName;
        this.mapMarkers = mapMarkers;
        this.boundingBox = boundingBox;
    }

    // Parcelling part
    public MapConfigurationV1(Parcel in) {
        this.currentServerTime = in.readLong();
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        in.readTypedList(this.files, OfflineMapFile.CREATOR);
        this.minZoom = in.readInt();
        this.maxZoom = in.readInt();
        this.version = in.readInt();
        this.mapSourceName = in.readString();
        this.boundingBox = in.readString();

        if (this.mapMarkers == null){
            this.mapMarkers = new HashMap<String, List<MapInfo>>();
        }
        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            final String key = in.readString();
            final int listLength = in.readInt();

            final List<MapInfo> list = new ArrayList<MapInfo>(listLength);
            for (int j = 0; j < listLength; j++) {
                final MapInfo value = in.readParcelable(MapInfo.class.getClassLoader());
                list.add(value);
            }
            mapMarkers.put(key, list);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.currentServerTime);
        if (this.files != null && this.files.size() > 0)
            dest.writeTypedList(this.files);
        else
            dest.writeTypedList(new ArrayList<>());
        dest.writeInt(this.minZoom);
        dest.writeInt(this.maxZoom);
        dest.writeInt(this.version);
        dest.writeString(this.mapSourceName);
        dest.writeString(this.boundingBox);

        if (mapMarkers != null && !mapMarkers.isEmpty()){
            dest.writeInt(mapMarkers.size());

            for (Map.Entry<String, List<MapInfo>> entry : mapMarkers.entrySet()) {
                dest.writeString(entry.getKey());

                final List<MapInfo> list = entry.getValue();
                final int listLength = list.size();
                dest.writeInt(listLength);
                for (MapInfo item: list) {
                    dest.writeParcelable(item, 0);
                }
            }
        }
    }

    public List<OfflineMapFile> getFiles() {
        return files;
    }

    public void setFiles(List<OfflineMapFile> files) {
        this.files = new ArrayList<>();
        this.files.addAll(files);
    }

    public Integer getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(Integer minZoom) {
        this.minZoom = minZoom;
    }

    public Integer getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(Integer maxZoom) {
        this.maxZoom = maxZoom;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public long getCurrentServerTime() {
        return currentServerTime;
    }

    public void setCurrentServerTime(long currentServerTime) {
        this.currentServerTime = currentServerTime;
    }

    public String getMapSourceName() {
        return mapSourceName;
    }

    public void setMapSourceName(String mapSourceName) {
        this.mapSourceName = mapSourceName;
    }


    public Map<String, List<MapInfo>> getMapMarkers() {
        return mapMarkers;
    }

    public void setMapMarkers(Map<String, List<MapInfo>> mapMarkers) {
        this.mapMarkers = mapMarkers;
    }

    public String getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }
}
