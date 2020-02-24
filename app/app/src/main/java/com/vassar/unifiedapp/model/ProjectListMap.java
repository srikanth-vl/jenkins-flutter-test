package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectListMap implements Parcelable {
    public static final Creator<ProjectListMap> CREATOR = new Creator<ProjectListMap>() {
        @Override
        public ProjectListMap createFromParcel(Parcel in) {
            return new ProjectListMap(in);
        }

        @Override
        public ProjectListMap[] newArray(int size) {
            return new ProjectListMap[size];
        }
    };
    @JsonProperty("map_layers_info")
    private List<MapInfo> layers;
    @JsonProperty("map_markers_info")
    private List<MapInfo> markers;
    @JsonProperty("map_legend")
    private MapLegend legend;

    public ProjectListMap() {
    }

    public ProjectListMap(List<MapInfo> layers, List<MapInfo> markers, MapLegend legend) {
        this.layers = layers;
        this.markers = markers;
        this.legend = legend;
    }

    protected ProjectListMap(Parcel in) {
    }

    public List<MapInfo> getLayers() {
        return layers;
    }

    public void setLayers(List<MapInfo> layers) {
        this.layers = layers;
    }

    public List<MapInfo> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MapInfo> markers) {
        this.markers = markers;
    }

    public MapLegend getLegend() {
        return legend;
    }

    public void setLegend(MapLegend legend) {
        this.legend = legend;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
