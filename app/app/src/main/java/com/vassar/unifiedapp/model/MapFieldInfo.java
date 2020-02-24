package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapFieldInfo implements Parcelable {

    public static final Creator<MapFieldInfo> CREATOR = new Creator<MapFieldInfo>() {
        @Override
        public MapFieldInfo createFromParcel(Parcel in) {
            return new MapFieldInfo(in);
        }

        @Override
        public MapFieldInfo[] newArray(int size) {
            return new MapFieldInfo[size];
        }
    };
    @JsonProperty("map_activity_type")
    private String mapActivityType;
    @JsonProperty("map_activity_layer")
    private List<String> mapActivityLayers;

    public MapFieldInfo() {
        mapActivityLayers = new ArrayList<>();
    }

    private MapFieldInfo(Parcel in) {
        this.mapActivityType = in.readString();
        if (mapActivityLayers != null) {
            int size = mapActivityLayers.size();
            in.readStringList(mapActivityLayers);
        } else {
            mapActivityLayers = new ArrayList<>();
            in.readStringList(mapActivityLayers);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mapActivityType);
        dest.writeStringList(this.mapActivityLayers);
    }

    public String getMapActivityType() {
        return mapActivityType;
    }

    public void setMapActivityType(String mapActivityType) {
        this.mapActivityType = mapActivityType;
    }

    public List<String> getMapActivityLayers() {
        return mapActivityLayers;
    }

    public void setMapActivityLayers(List<String> mapActivityLayers) {
        this.mapActivityLayers = mapActivityLayers;
    }
}
