package com.vassar.unifiedapp.model;

public class LatestFieldValue {

    String key;
    String label;
    String value;

    public LatestFieldValue(String key, String label, String value) {
        this.key = key;
        this.label = label;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "LatestFieldValue{" +
                "key='" + key + '\'' +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
