package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapLegend {
    @JsonProperty("legend_visibility")
    private boolean isLegendVisible;
    @JsonProperty("legend_orientation")
    private String legendOrientation;

    public MapLegend() {
    }

    public MapLegend(boolean isLegendVisible, String legendOrientation) {
        this.isLegendVisible = isLegendVisible;
        this.legendOrientation = legendOrientation;
    }

    public boolean isLegendVisible() {
        return isLegendVisible;
    }

    public void setLegendVisible(boolean legendVisible) {
        isLegendVisible = legendVisible;
    }

    public String getLegendOrientation() {
        return legendOrientation;
    }

    public void setLegendOrientation(String legendOrientation) {
        this.legendOrientation = legendOrientation;
    }

    @Override
    public String toString() {
        return "MapLegend{" +
                "isLegendVisible='" + isLegendVisible + '\'' +
                ", legendOrientation=" + legendOrientation +
                '}';
    }
}
